package com.chatcut.gateway.filter;

import cn.dev33.satoken.stp.StpLogic;
import com.chatcut.common.security.InternalSign;
import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.StpUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 网关鉴权（粗粒度）：
 * 1) 白名单直接放行；
 * 2) 解析 Bearer token，按路径判断用 user 还是 admin 体系；
 * 3) 剥离外部传入的身份头，重新写入 X-User-Id / X-Admin-Id + HMAC 签名；
 * 4) 生成 traceId 透传给下游。
 * 细粒度权限在业务服务里做（@SaCheckPermission），网关不做。
 */
@Slf4j
@Component
public class AuthWebFilter implements GlobalFilter, Ordered {

    // Ant 风格路径匹配器，无状态可复用
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /**
     * 白名单可经 Nacos 覆盖（chatcut.gateway.whitelist，逗号分隔）。
     * 默认含登录路径与 swagger/knife4j 文档路径（仅联调用，测试/生产可在 Nacos 里去掉文档路径）。
     */
    @Value("${chatcut.gateway.whitelist:"
            + "/api/auth/**,/admin/auth/**,/actuator/**,/internal/health,"
            + "/api/v3/api-docs/**,/api/swagger-ui/**,/api/webjars/**,/api/doc.html,"
            + "/admin/v3/api-docs/**,/admin/swagger-ui/**,/admin/webjars/**,/admin/doc.html}")
    private String whitelistConfig;

    /** 启动时把 whitelistConfig 按逗号解析成列表，避免每次请求重复切分 */
    private List<String> whiteList;

    @jakarta.annotation.PostConstruct
    void initWhitelist() {
        whiteList = java.util.Arrays.stream(whitelistConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    // 网关注入下游的内部头：身份 + 时间戳 + HMAC 签名 + 链路追踪
    private static final String H_USER = "X-User-Id";
    private static final String H_ADMIN = "X-Admin-Id";
    private static final String H_TS = "X-Request-Ts";
    private static final String H_SIGN = "X-Internal-Sign";
    private static final String H_TRACE = "X-Trace-Id";

    /** 内部签名密钥；为空则跳过签名（一般只在本机裸跑时允许） */
    @Value("${chatcut.security.internal-secret:}")
    private String internalSecret;

    /** 网关鉴权主流程：放行白名单 → 校验 token → 覆盖内部头透传下游。 */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        // 复用上游已带的 traceId，没有则生成 16 位短码，保证全链路同一标识
        String traceId = StringUtils.hasText(exchange.getRequest().getHeaders().getFirst(H_TRACE))
                ? exchange.getRequest().getHeaders().getFirst(H_TRACE)
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 白名单不校验登录，身份头留空（下游按匿名处理）
        if (isWhiteListed(path)) {
            return chain.filter(withTrace(exchange, traceId, null, null));
        }

        String token = resolveToken(exchange.getRequest().getHeaders());
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, traceId, "missing token");
        }

        // 按路径前缀区分体系：/admin/ 走后台 token，其余走 C 端用户 token
        boolean adminPath = path.startsWith("/admin/");
        StpLogic logic = adminPath ? StpAdminUtil.logic() : StpUserUtil.logic();
        Object loginId;
        try {
            loginId = logic.getLoginIdByToken(token);
        } catch (Exception e) {
            // token 非法/解析异常一律当作未登录，统一返回 401
            log.warn("token resolve failed, path={}", path, e);
            loginId = null;
        }
        if (loginId == null) {
            return unauthorized(exchange, traceId, "invalid token");
        }

        // userId 与 adminId 互斥，只填其一，避免同一请求冒充两种身份
        String userId = adminPath ? null : String.valueOf(loginId);
        String adminId = adminPath ? String.valueOf(loginId) : null;
        return chain.filter(withTrace(exchange, traceId, userId, adminId));
    }

    /** 覆盖（而不是新增）身份头，避免客户端伪造 X-User-Id 一路透到业务服务。 */
    private ServerWebExchange withTrace(ServerWebExchange exchange, String traceId, String userId, String adminId) {
        // 时间戳参与签名，下游可据此校验时效、防重放
        long ts = System.currentTimeMillis();
        String sign = StringUtils.hasText(internalSecret)
                ? InternalSign.sign(userId, adminId, String.valueOf(ts), internalSecret)
                : "";

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    // 先清空外部可能伪造的身份/签头，再写入网关可信值
                    headers.remove(H_USER);
                    headers.remove(H_ADMIN);
                    headers.remove(H_TS);
                    headers.remove(H_SIGN);
                    headers.set(H_TRACE, traceId);
                    if (userId != null) {
                        headers.set(H_USER, userId);
                    }
                    if (adminId != null) {
                        headers.set(H_ADMIN, adminId);
                    }
                    // 未配置密钥时不写签名头，下游应拒绝这类内部请求
                    if (StringUtils.hasText(sign)) {
                        headers.set(H_TS, String.valueOf(ts));
                        headers.set(H_SIGN, sign);
                    }
                })
                .build();
        // 回写响应头，便于客户端报障时按 traceId 定位
        exchange.getResponse().getHeaders().set(H_TRACE, traceId);
        return exchange.mutate().request(request).build();
    }

    private boolean isWhiteListed(String path) {
        return whiteList.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    /** 仅支持 Authorization: Bearer 形式，返回去掉前缀后的 token。 */
    private String resolveToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            // "Bearer " 占 7 个字符，切掉前缀即为 token
            return authorization.substring(7).trim();
        }
        return null;
    }

    /** 统一 401 响应体，code 与业务错误码（登录失效）对齐，前端可据此跳登录。 */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String traceId, String reason) {
        log.warn("unauthorized request path={} reason={}", exchange.getRequest().getPath().value(), reason);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set(H_TRACE, traceId);
        String body = "{\"code\":10004,\"msg\":\"登录已失效，请重新登录\",\"traceId\":\"" + traceId + "\"}";
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    /** 优先级很高，确保在路由转发前完成鉴权与头覆盖。 */
    @Override
    public int getOrder() {
        return -100;
    }
}
