package com.chatcut.common.security.web;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.StpUserUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注解鉴权注册：
 * - C 端服务（chatcut.security.type=user）校验 user token；
 * - 管理端服务（type=admin）校验 admin token；
 * 白名单放登录与健康检查，其余一律需要登录。
 *
 * <p>白名单只写具体路径，不要用 {@code /auth/**} 这类前缀：那会把 {@code /auth/me}、
 * {@code /auth/logout} 一起放行，未登录也能进，取到的身份是 null。
 * 新增免登录接口（如短信验证码）时必须在这里显式补一行。</p>
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

    @Value("${chatcut.security.type:user}")
    private String type;

    private static final String[] WHITE_LIST = {
            "/internal/health",
            "/actuator/**",
            "/v3/api-docs/**",
            // knife4j 增强文档页；其静态资源走 /webjars/**，spec 仍取 /v3/api-docs
            "/doc.html",
            // springdoc 的短链 /swagger-ui.html 是 302 跳 /swagger-ui/index.html，
            // 不放行的话会在跳转前就被拦成 401（很容易被误判成"swagger 挂了"）。
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**",
            // 浏览器自动请求，拦成 401 只会在控制台留噪音
            "/favicon.ico",
            "/auth/login"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if ("admin".equalsIgnoreCase(type)) {
            registry.addInterceptor(new SaInterceptor(handle -> StpAdminUtil.logic().checkLogin()))
                    .addPathPatterns("/**")
                    .excludePathPatterns(WHITE_LIST);
        } else {
            registry.addInterceptor(new SaInterceptor(handle -> StpUserUtil.logic().checkLogin()))
                    .addPathPatterns("/**")
                    .excludePathPatterns(WHITE_LIST);
        }
    }
}
