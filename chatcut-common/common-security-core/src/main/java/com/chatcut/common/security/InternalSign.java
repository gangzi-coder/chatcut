package com.chatcut.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 网关 → 业务服务的内部身份签名。
 * 只带 header 不带签名等于谁都能伪造 X-User-Id，这一步不能省。
 */
public final class InternalSign {

    private static final String ALGO = "HmacSHA256";

    private InternalSign() {
    }

    public static String sign(String userId, String adminId, String timestamp, String secret) {
        String raw = nullToEmpty(userId) + "|" + nullToEmpty(adminId) + "|" + timestamp;
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGO));
            return HexFormat.of().formatHex(mac.doFinal(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("sign failed", e);
        }
    }

    public static boolean verify(String userId, String adminId, String timestamp, String sign, String secret) {
        String expected = sign(userId, adminId, timestamp, secret);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sign.getBytes(StandardCharsets.UTF_8));
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }
}
