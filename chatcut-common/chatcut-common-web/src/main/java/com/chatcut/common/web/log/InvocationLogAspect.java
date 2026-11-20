package com.chatcut.common.web.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 出入参日志切面。日志格式对齐 EasyMS 约定：结构化【键值对】+ 分阶段语义标签，
 * 一次请求在日志里是三段式（请求/入参 → 响应 → 或异常），traceId 由 MDC 统一输出在行首：
 *
 * <pre>
 * 【服务入口-请求】-【服务: chatcut-app-admin】-【实例: 10.0.42.145:9101】-【调用方: chatcut-gateway】-【请求IP: 10.0.42.145】-【请求地址: /auth/login】-【请求方法: POST】-【方法: AdminAuthController#login】-【发起时间: 2026-11-13 14:00:00.123】
 * 【服务入口-入参】{"req":{"username":"admin","password":"***"}}
 * 【服务入口-响应】-【方法: AdminAuthController#login】-【请求地址: /auth/login】-【响应时间: 2026-11-13 14:00:00.306】-【用时: 183ms】-【响应数据: {...}】
 * 【服务入口-异常】-【方法: RoleController#roleDetail】-【请求地址: /system/roles/1】-【异常类型: BizException】-【异常信息: 角色不存在】-【用时: 4ms】
 * </pre>
 *
 * <p>约定：
 * <ul>
 *   <li>controller 层（com.chatcut..controller..*）按包路径自动切入，标签为「服务入口」；service 层用 {@link LogInvocation}
 *       手动标记，标签为「方法调用」；</li>
 *   <li>入参/出参用独立的 ObjectMapper 序列化，敏感字段（黑名单）脱敏为 "***"，不截断；</li>
 *   <li>耗时超过 slow-threshold-ms 时响应行升级为 WARN（内容不变，靠级别区分告警）；</li>
 *   <li>异常行原样 rethrow，绝不吞异常；日志相关的任何异常（序列化失败等）只降级为占位文本，绝不影响业务执行。</li>
 * </ul>
 *
 * <p>已知限制（排查"没打印"时先看这几条）：
 * 同类自调用（this.b()）不触发切面；final 方法/类不生效；
 * 网关是 WebFlux，不引入本模块，它的路由日志由 chatcut-gateway 的 AccessLogWebFilter 负责；
 * 路径参数绑定失败发生在进 controller 之前，切面拦不到。</p>
 */
@Slf4j
@Aspect
@Component
public class InvocationLogAspect {

    /** controller 层入口标签 / service 层手动标记标签。 */
    private static final String TAG_ENTRY = "服务入口";
    private static final String TAG_METHOD = "方法调用";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 序列化时直接跳过的类型：进 JSON 会炸栈或打出几 MB 的流信息。 */
    private static final Set<Class<?>> SKIP_TYPES = Set.of(
            HttpServletRequest.class, HttpServletRequestWrapper.class, HttpServletResponse.class,
            HttpSession.class, MultipartFile.class, BindingResult.class, Principal.class,
            Model.class, ModelMap.class,
            InputStream.class, OutputStream.class);

    private final InvocationLogProperties props;
    private final ObjectMapper logMapper;
    private final String appName;

    /**
     * 当前实例标识 ip:port。多实例部署（同一服务起多份）时，靠它在日志里分清请求落到了哪一台；
     * 仅在容器真正监听后由 WebServerInitializedEvent 写入，之前记为 "-"。
     */
    private volatile String instanceId = "-";

    public InvocationLogAspect(InvocationLogProperties props, Environment env) {
        this.props = props;
        this.appName = env.getProperty("spring.application.name", "unknown");
        // 日志专用 mapper：不碰全局 ObjectMapper，脱敏模块只作用于这里。
        this.logMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SensitiveFieldModule(
                        props.getSensitiveFields().stream().map(String::toLowerCase).collect(Collectors.toSet())))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /** 容器起来后才知道真实端口（server.port=0 时配置值不可信），此时固定实例标识。 */
    @EventListener
    public void onWebServerReady(WebServerInitializedEvent event) {
        this.instanceId = localIp() + ":" + event.getWebServer().getPort();
    }

    /**
     * 取本机对外 IP。getLocalHost 在部分环境（hosts 把主机名指到 127.0.0.1）会返回回环地址，
     * 与网关日志/Nacos 注册的 IP 对不上，所以回环时退回遍历网卡取第一个非回环 IPv4。
     */
    private String localIp() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (!local.isLoopbackAddress() && !local.isAnyLocalAddress()) {
                return local.getHostAddress();
            }
        } catch (Exception ignored) {
            // 继续走网卡兜底
        }
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
            // 拿不到就返回 unknown，端口仍可用于区分实例
        }
        return "unknown";
    }

    @Pointcut("within(com.chatcut..controller..*)")
    public void controllerLayer() {
    }
    @Pointcut("@annotation(com.chatcut.common.web.log.LogInvocation)")
    public void annotated() {
    }

    @Around("controllerLayer()")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        return doLog(pjp, TAG_ENTRY);
    }

    @Around("annotated()")
    public Object aroundAnnotated(ProceedingJoinPoint pjp) throws Throwable {
        return doLog(pjp, TAG_METHOD);
    }

    private Object doLog(ProceedingJoinPoint pjp, String tag) throws Throwable {
        if (!props.isEnabled()) {
            return pjp.proceed();
        }
        HttpServletRequest request = currentRequest();
        if (request != null && isExcluded(request.getRequestURI())) {
            return pjp.proceed();
        }

        String method = signature(pjp);
        String uri = request != null ? request.getRequestURI() : "-";
        long start = System.nanoTime();
        String now = LocalDateTime.now().format(TS);

        // 请求 + 入参两行；日志自身的任何失败都只降级，不影响业务。
        try {
            String ip = request != null ? clientIp(request) : "-";
            String httpMethod = request != null ? request.getMethod() : "-";
            log.info("【{}-请求】-【服务: {}】-【实例: {}】-【调用方: {}】-【请求IP: {}】-【请求地址: {}】-【请求方法: {}】-【方法: {}】-【发起时间: {}】",
                    tag, appName, instanceId, caller(request), ip, uri, httpMethod, method, now);
        } catch (Exception e) {
            log.warn("【{}-请求】-【服务: {}】-【实例: {}】-【方法: {}】-【发起时间: {}】-【日志组装失败: {}】",
                    tag, appName, instanceId, method, now, e.getClass().getSimpleName());
        }
        try {
            log.info("【{}-入参】{}", tag, safeArgs(pjp));
        } catch (Exception e) {
            log.warn("【{}-入参】<log-failed:{}>", tag, e.getClass().getSimpleName());
        }

        try {
            Object ret = pjp.proceed();
            long cost = costMs(start);
            String line = "【{}-响应】-【方法: {}】-【请求地址: {}】-【响应时间: {}】-【用时: {}ms】-【响应数据: {}】";
            if (cost >= props.getSlowThresholdMs()) {
                log.warn(line, tag, method, uri, LocalDateTime.now().format(TS), cost, safeValue(ret));
            } else {
                log.info(line, tag, method, uri, LocalDateTime.now().format(TS), cost, safeValue(ret));
            }
            return ret;
        } catch (Throwable e) {
            long cost = costMs(start);
            log.error("【{}-异常】-【方法: {}】-【请求地址: {}】-【异常类型: {}】-【异常信息: {}】-【用时: {}ms】",
                    tag, method, uri, e.getClass().getSimpleName(), e.getMessage(), cost);
            throw e;
        }
    }

    private boolean isExcluded(String uri) {
        for (String p : props.getExcludePaths()) {
            if (PATH_MATCHER.match(p, uri)) {
                return true;
            }
        }
        return false;
    }

    private String signature(ProceedingJoinPoint pjp) {
        Method m = ((MethodSignature) pjp.getSignature()).getMethod();
        return m.getDeclaringClass().getSimpleName() + "#" + m.getName();
    }

    private long costMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    /** 经网关的请求取 X-Forwarded-For 第一段，直连取 remoteAddr。 */
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 调用方：网关会在转发前写入 X-Caller（值为网关的 spring.application.name），
     * 没有这个头说明是绕过网关直连（本地联调/内部调用），记为 direct。
     * 该字段只用于日志，不参与任何鉴权判断。
     */
    private String caller(HttpServletRequest request) {
        if (request == null) {
            return "-";
        }
        String c = request.getHeader("X-Caller");
        return StringUtils.hasText(c) ? c : "direct";
    }

    @SuppressWarnings("unchecked")
    private String safeArgs(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "{}";
        }
        String[] names = ((MethodSignature) pjp.getSignature()).getParameterNames();
        Map<String, Object> named = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            named.put(names != null && i < names.length ? names[i] : "arg" + i, clean(args[i]));
        }
        return write(named);
    }

    private String safeValue(Object ret) {
        return write(clean(ret));
    }

    /** 清洗单个值：跳过不可序列化类型，其余交给 mapper（脱敏在序列化层完成）。 */
    private Object clean(Object v) {
        if (v == null) {
            return null;
        }
        for (Class<?> t : SKIP_TYPES) {
            if (t.isInstance(v)) {
                return describe(v);
            }
        }
        return v;
    }

    private String describe(Object v) {
        if (v instanceof MultipartFile f) {
            return "<file name=" + f.getOriginalFilename() + " size=" + f.getSize() + ">";
        }
        if (v instanceof Collection<?> c) {
            return "<collection size=" + c.size() + ">";
        }
        return "<" + v.getClass().getSimpleName() + ">";
    }

    private String write(Object v) {
        try {
            return logMapper.writeValueAsString(v);
        } catch (Exception e) {
            return "<unprintable:" + e.getClass().getSimpleName() + ">";
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attr) {
            return attr.getRequest();
        }
        return null;
    }
}
