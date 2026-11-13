package com.chatcut.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 网关访问日志。日志格式与业务服务的出入参切面（InvocationLogAspect）保持同一套【键值对】约定：
 *
 * <pre>
 * 【网关路由-请求】-【服务: chatcut-gateway】-【路由: app-admin】-【目标服务: chatcut-app-admin】-【请求IP: 127.0.0.1】-【请求地址: /admin/auth/login】-【请求方法: POST】-【发起时间: 2026-11-13 15:40:00.123】
 * 【网关路由-响应】-【路由: app-admin】-【目标实例: 10.0.42.145:9101】-【状态码: 200】-【用时: 24ms】
 * 【网关路由-异常】-【路由: app-admin】-【异常类型: ConnectException】-【异常信息: Connection refused】-【用时: 3002ms】
 * </pre>
 *
 * <p>职责：
 * <ul>
 *   <li>链路起点：生成（或复用上游传来的）traceId 写入请求头 X-Trace-Id，下游 TraceIdFilter 直接复用，
 *       保证网关/业务服务打印的是同一个 traceId；</li>
 *   <li>标注调用方：写入 X-Caller，业务服务日志里据此区分「经网关」与「直连」；</li>
 *   <li>目标实例：响应阶段从 GATEWAY_REQUEST_URL_ATTR 读出负载均衡后真正转发到的 ip:port，
 *       多实例部署时能看清请求落到了哪一台。</li>
 * </ul>
 *
 * <p>不打印请求/响应体：网关拿到的是流，读 body 需要额外缓存，代价大且容易污染下游。
 * actuator 等路径默认排除，避免健康检查刷屏。
 */
@Slf4j
@Component
public class AccessLogWebFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** 与 TraceContext.HEADER / AuthWebFilter 保持一致：全链路只认这一个头。 */
    private static final String H_TRACE = "X-Trace-Id";
    /** 调用方标识，业务服务切面读取后打进【调用方】字段。 */
    private static final String H_CALLER = "X-Caller";

    private static final String MDC_KEY = "traceId";

    @Value("${spring.application.name:gateway}")
    private String appName;

    @Value("${chatcut.log.gateway.enabled:true}")
    private boolean enabled;

    /** 排除路径（Ant 风格，逗号分隔）：健康检查、文档等不该刷进业务日志的请求。 */
    @Value("${chatcut.log.gateway.exclude-paths:/actuator/**,/favicon.ico}")
    private String excludeConfig;

    private List<String> excludePaths;

    @jakarta.annotation.PostConstruct
    void init() {
        this.excludePaths = Arrays.stream(excludeConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!enabled || isExcluded(path)) {
            return chain.filter(exchange);
        }

        // 复用上游 traceId（例如前端/压测工具自带），没有则生成 16 位短码；
        // 必须在 AuthWebFilter(-100) 之前落到请求头上，它内部同样是「有则复用」。
        // 声明成 final：下面的响应/异常回调（lambda）要捕获它。
        String incoming = exchange.getRequest().getHeaders().getFirst(H_TRACE);
        final String traceId = StringUtils.hasText(incoming)
                ? incoming
                : UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.set(H_TRACE, traceId);
                    // 覆盖而不是新增：客户端自己带的 X-Caller 不可信，只用于日志，仍以网关为准
                    headers.set(H_CALLER, appName);
                })
                .build();
        ServerWebExchange ex = exchange.mutate().request(request).build();
        // 回写响应头，客户端报障时报这个 traceId 就能串起整条链路
        ex.getResponse().getHeaders().set(H_TRACE, traceId);

        Route route = ex.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : "-";
        String targetService = targetService(route);
        long start = System.nanoTime();

        mdLog(traceId, "INFO", "【网关路由-请求】-【服务: " + appName + "】-【路由: " + routeId
                + "】-【目标服务: " + targetService + "】-【请求IP: " + clientIp(ex) + "】-【请求地址: " + path
                + "】-【请求方法: " + method(ex) + "】-【发起时间: " + now() + "】");

        return chain.filter(ex)
                .doOnSuccess(ignored -> {
                    HttpStatus status = ex.getResponse().getStatusCode() instanceof HttpStatus s ? s : null;
                    int code = status != null ? status.value() : 200;
                    // 4xx/5xx 说明网关或下游出了问题，用 WARN 让告警规则只盯异常请求
                    String level = code >= 400 ? "WARN" : "INFO";
                    mdLog(traceId, level, "【网关路由-响应】-【路由: " + routeId + "】-【目标实例: " + instance(ex)
                            + "】-【状态码: " + code + "】-【用时: " + costMs(start) + "ms】");
                })
                .doOnError(e -> mdLog(traceId, "ERROR", "【网关路由-异常】-【路由: " + routeId
                        + "】-【目标实例: " + instance(ex) + "】-【异常类型: " + e.getClass().getSimpleName()
                        + "】-【异常信息: " + e.getMessage() + "】-【用时: " + costMs(start) + "ms】"));
    }

    /** 路由配置里的目标服务名：lb://chatcut-app-admin → chatcut-app-admin。 */
    private String targetService(Route route) {
        if (route == null) {
            return "-";
        }
        URI uri = route.getUri();
        return StringUtils.hasText(uri.getHost()) ? uri.getHost() : uri.toString();
    }

    /** 负载均衡后真正转发到的实例；未完成选路（如鉴权阶段就返回 401）时为 "-"。 */
    private String instance(ServerWebExchange exchange) {
        URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        if (url == null || url.getHost() == null) {
            return "-";
        }
        return url.getHost() + ":" + (url.getPort() > 0 ? url.getPort() : defaultPort(url.getScheme()));
    }

    private int defaultPort(String scheme) {
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    private String clientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "-";
    }

    private String method(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod() != null ? exchange.getRequest().getMethod().name() : "-";
    }

    private boolean isExcluded(String path) {
        return excludePaths.stream().anyMatch(p -> MATCHER.match(p, path));
    }

    private long costMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String now() {
        return LocalDateTime.now().format(TS);
    }

    /**
     * 写带 traceId 的日志。WebFlux 是异步的，MDC 不会跨线程自动传播，
     * 所以只在打印这一瞬间 put/remove，避免污染 Netty 线程池里的后续请求。
     */
    private void mdLog(String traceId, String level, String message) {
        MDC.put(MDC_KEY, traceId);
        try {
            switch (level) {
                case "WARN" -> log.warn(message);
                case "ERROR" -> log.error(message);
                default -> log.info(message);
            }
        } finally {
            MDC.remove(MDC_KEY);
        }
    }

    /** 最外层：先于鉴权与选路，保证 traceId / X-Caller 对所有分支（含 401 短路）都生效。 */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
