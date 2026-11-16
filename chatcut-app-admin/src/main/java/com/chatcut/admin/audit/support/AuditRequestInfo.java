package com.chatcut.admin.audit.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 从当前请求取审计所需的上下文信息。取不到（定时任务、单测）时返回 "-"，不抛异常。
 */
final class AuditRequestInfo {

    /** 网关写入的调用方标识，见 chatcut-gateway 的 AccessLogWebFilter。 */
    private static final String H_CALLER = "X-Caller";

    private AuditRequestInfo() {
    }

    static String ip() {
        String forwarded = header("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        HttpServletRequest request = request();
        return request == null ? "-" : request.getRemoteAddr();
    }

    static String userAgent() {
        String ua = header("User-Agent");
        return StringUtils.hasText(ua) && ua.length() > 255 ? ua.substring(0, 255) : (ua == null ? "-" : ua);
    }

    static String caller() {
        String caller = header(H_CALLER);
        return StringUtils.hasText(caller) ? caller : "direct";
    }

    static String uri() {
        HttpServletRequest request = request();
        return request == null ? "-" : request.getRequestURI();
    }

    static String httpMethod() {
        HttpServletRequest request = request();
        return request == null ? "-" : request.getMethod();
    }

    private static String header(String name) {
        HttpServletRequest request = request();
        return request == null ? null : request.getHeader(name);
    }

    private static HttpServletRequest request() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes servletAttrs ? servletAttrs.getRequest() : null;
    }
}
