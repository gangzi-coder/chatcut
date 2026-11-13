package com.chatcut.common.security.web;

import com.chatcut.common.core.trace.TraceContext;
import com.chatcut.common.security.InternalSign;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 校验网关写入的 X-Internal-Sign，防止绕过网关直接伪造 X-User-Id。
 * chatcut.security.internal-secret 为空时跳过（仅限本地开发）。
 */
@Component
@Order(1)
public class InternalSignFilter extends OncePerRequestFilter {

    public static final String H_USER = "X-User-Id";
    public static final String H_ADMIN = "X-Admin-Id";
    public static final String H_TS = "X-Request-Ts";
    public static final String H_SIGN = "X-Internal-Sign";

    @Value("${chatcut.security.internal-secret:}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!StringUtils.hasText(secret)) {
            chain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(H_USER);
        String adminId = request.getHeader(H_ADMIN);
        String ts = request.getHeader(H_TS);
        String sign = request.getHeader(H_SIGN);

        if (StringUtils.hasText(userId) || StringUtils.hasText(adminId)) {
            boolean valid = StringUtils.hasText(ts)
                    && StringUtils.hasText(sign)
                    && Math.abs(System.currentTimeMillis() - Long.parseLong(ts)) < 60_000
                    && InternalSign.verify(userId, adminId, ts, sign, secret);
            if (!valid) {
                logger.warn("invalid internal sign, uri=" + request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":10004,\"msg\":\"invalid internal signature\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
