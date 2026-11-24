package com.chatcut.portal.website.support;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.chatcut.common.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

/**
 * 一次性验证码凭据管理（HMAC-SHA256 签名 + Redis 原子核销标记，多实例部署安全）。
 *
 * <p>凭据成对下发：requestId（16 位随机）内嵌进令牌 payload 并由 HMAC 签名覆盖，
 * 服务端无状态即可校验「token 与 requestId 配对 + 签名合法 + 未过期」，
 * 无需维护已下发清单。</p>
 *
 * <p>令牌格式：{@code base64url(payload).base64url(hmac)}，payload 为
 * {@code 过期时间戳(base36).requestId(16)}，HMAC-SHA256 截断 20 字节，
 * 总长 ≤ 64 字符，与 crm_lead_submission.captcha_token VARCHAR(64) 匹配。</p>
 *
 * <p>「一次性」语义分层保证（均为跨实例生效）：
 * <ul>
 *   <li>Redis 核销标记（token 与 requestId 各一条，TTL 对齐有效期+缓冲）：
 *       提交成功后原子标记，全局只可用一次，覆盖重启与多实例场景；</li>
 *   <li>留资：crm_lead_submission.captcha_token / request_id 双唯一键兜底（数据库权威）；</li>
 *   <li>反馈：crm_feedback.request_id 唯一键兜底同请求重放。</li>
 * </ul>
 * Redis 不可用时核销校验降级放行，由上述唯一键兜底，不阻断提交主链路。</p>
 */
@Slf4j
@Component
public class CaptchaTokenManager {

    /** HMAC 截断字节数：20 字节 base64url 后 27 字符，保证令牌总长 ≤ 64。 */
    private static final int MAC_BYTES = 20;

    /** payload 分隔符。 */
    private static final char SEPARATOR = '.';

    /** requestId 随机段长度（16 位，保证令牌总长 ≤ 64）。 */
    private static final int REQUEST_ID_LENGTH = 16;

    /** Redis 核销标记 key 前缀：令牌。 */
    private static final String USED_TOKEN_KEY_PREFIX = "portal:website:captcha:used:token:";

    /** Redis 核销标记 key 前缀：requestId。 */
    private static final String USED_REQUEST_KEY_PREFIX = "portal:website:captcha:used:request:";

    /** 核销标记 TTL 缓冲分钟数：令牌有效期外多留 5 分钟，防止边界并发窗口。 */
    private static final long CONSUMED_TTL_BUFFER_MINUTES = 5L;

    private final PortalWebsiteProperties properties;

    public CaptchaTokenManager(PortalWebsiteProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成一次性验证码凭据对（requestId + captchaToken，requestId 签进令牌），不发送短信/邮件。
     */
    public CaptchaPair generate() {
        String requestId = RandomUtil.randomString(REQUEST_ID_LENGTH);
        long expiresAt = System.currentTimeMillis() + properties.getCaptcha().getTtlMinutes() * 60_000L;
        String payload = Long.toString(expiresAt, Character.MAX_RADIX) + SEPARATOR + requestId;
        byte[] mac = sign(payload);
        String token = encodeUrl(payload.getBytes(StandardCharsets.UTF_8)) + SEPARATOR + encodeUrl(mac);
        return new CaptchaPair(requestId, token);
    }

    /**
     * 校验凭据对：令牌签名合法、未过期、且与提交的 requestId 配对（不校验是否已消费，见 {@link #isConsumed}）。
     *
     * @return true 表示凭据对合法且未过期
     */
    public boolean verifyPair(String token, String requestId) {
        String embeddedRequestId = resolveRequestId(token);
        if (embeddedRequestId == null) {
            return false;
        }
        // 常量时间比较，防时序攻击
        return requestId != null && !requestId.isBlank()
            && MessageDigest.isEqual(embeddedRequestId.getBytes(StandardCharsets.UTF_8),
                requestId.getBytes(StandardCharsets.UTF_8));
    }

    /** 令牌是否已被消费（token 或 requestId 任一已核销即为已使用；Redis 标记，多实例共享）。 */
    public boolean isConsumed(String token, String requestId) {
        try {
            if (token != null && !token.isBlank()
                && Boolean.TRUE.equals(RedisUtils.hasKey(usedTokenKey(token)))) {
                return true;
            }
            return requestId != null && !requestId.isBlank()
                && Boolean.TRUE.equals(RedisUtils.hasKey(usedRequestKey(requestId)));
        } catch (Exception e) {
            log.warn("验证码凭据核销标记查询失败，降级放行（唯一键兜底），requestId={}", requestId, e);
            return false;
        }
    }

    /**
     * 标记凭据对已消费（token 与 requestId 各写一条 Redis 标记，
     * TTL 对齐令牌有效期 + 缓冲，到期自动清理无需手工维护）。
     *
     * <p>Redis 不可用时降级跳过：留资侧双唯一键仍权威兜底，
     * 反馈侧退化为 request_id 唯一键兜底，不阻断已成功写库的提交。</p>
     */
    public void consume(String token, String requestId) {
        try {
            long ttlMinutes = properties.getCaptcha().getTtlMinutes() + CONSUMED_TTL_BUFFER_MINUTES;
            Duration ttl = Duration.ofMinutes(ttlMinutes);
            long now = System.currentTimeMillis();
            if (token != null && !token.isBlank()) {
                RedisUtils.setCacheObject(usedTokenKey(token), now, ttl);
            }
            if (requestId != null && !requestId.isBlank()) {
                RedisUtils.setCacheObject(usedRequestKey(requestId), now, ttl);
            }
        } catch (Exception e) {
            log.warn("验证码凭据核销标记写入失败，留资侧由唯一键权威兜底，requestId={}", requestId, e);
        }
    }

    /** 客户端 IP HMAC 摘要（写入 crm_lead_submission.ip_hash，仅用于限流/审计）。 */
    public byte[] ipHash(String clientIp) {
        return sign(clientIp == null ? "unknown" : clientIp);
    }

    /**
     * 校验令牌签名与有效期并解析内嵌 requestId。
     *
     * @return 内嵌的 requestId；签名非法/过期/格式错误返回 null
     */
    private String resolveRequestId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }
        byte[] payloadBytes;
        byte[] macBytes;
        try {
            payloadBytes = decodeUrl(parts[0]);
            macBytes = decodeUrl(parts[1]);
        } catch (Exception e) {
            return null;
        }
        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        // 常量时间比较，防时序攻击
        if (!MessageDigest.isEqual(macBytes, sign(payload))) {
            return null;
        }
        int separatorIndex = payload.indexOf(SEPARATOR);
        if (separatorIndex <= 0) {
            return null;
        }
        try {
            long expiresAt = Long.parseLong(payload.substring(0, separatorIndex), Character.MAX_RADIX);
            if (expiresAt <= System.currentTimeMillis()) {
                return null;
            }
            return payload.substring(separatorIndex + 1);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 令牌核销标记 Redis key。 */
    private static String usedTokenKey(String token) {
        return USED_TOKEN_KEY_PREFIX + token;
    }

    /** requestId 核销标记 Redis key。 */
    private static String usedRequestKey(String requestId) {
        return USED_REQUEST_KEY_PREFIX + requestId;
    }

    /** HMAC-SHA256 截断签名。每次新建 HMac（javax.crypto.Mac 非线程安全）。 */
    private byte[] sign(String data) {
        HMac hMac = new HMac(HmacAlgorithm.HmacSHA256,
            properties.getCaptcha().getSecretKey().getBytes(StandardCharsets.UTF_8));
        byte[] full = hMac.digest(data.getBytes(StandardCharsets.UTF_8));
        byte[] truncated = new byte[MAC_BYTES];
        System.arraycopy(full, 0, truncated, 0, MAC_BYTES);
        return truncated;
    }

    /** base64url 无填充编码（JDK 标准库）。 */
    private static String encodeUrl(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** base64url 解码（JDK 标准库）。 */
    private static byte[] decodeUrl(String text) {
        return Base64.getUrlDecoder().decode(text);
    }
}
