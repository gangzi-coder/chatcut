package com.chatcut.common.security.web;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpLogic;
import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.StpEnterpriseUtil;
import com.chatcut.common.security.StpUserUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注解鉴权注册，按 {@code chatcut.security.type} 选择账号体系：
 * <ul>
 *   <li>{@code user} —— C 端服务，校验 user token；</li>
 *   <li>{@code enterprise} —— B 端服务，校验 enterprise token；</li>
 *   <li>{@code admin} —— 管理端服务，校验 admin token；</li>
 *   <li>{@code none} —— 完全免登录（官网门户这类只读服务），不注册任何拦截器。</li>
 * </ul>
 * 白名单放登录与健康检查，其余一律需要登录。
 *
 * <p>白名单只写具体路径，不要用 {@code /auth/**} 这类前缀：那会把 {@code /auth/me}、
 * {@code /auth/logout} 一起放行，未登录也能进，取到的身份是 null。
 * 新增免登录接口（如短信验证码）时必须在这里显式补一行。</p>
 */
@Configuration
public class SaTokenWebConfig implements WebMvcConfigurer {

    /** 支持的账号体系标识，见类注释。 */
    private static final java.util.Set<String> SUPPORTED = java.util.Set.of("user", "enterprise", "admin", "none");

    @Value("${chatcut.security.type:user}")
    private String type;

    /** B 端企业登录也走统一登录路径，白名单同样放行。 */

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
        // 官网门户这类免登录服务：不注册拦截器，也不做任何 token 校验
        if ("none".equalsIgnoreCase(type)) {
            return;
        }
        if (!SUPPORTED.contains(type.toLowerCase())) {
            throw new IllegalStateException("chatcut.security.type 只支持 user/enterprise/admin/none，当前是 " + type);
        }
        // 注意：这里只注册 lambda，不能在本方法里解析 StpLogic——
        // StpLogic 由 StpLogicConfig 的 SmartInitializingSingleton 绑定，时机晚于本方法；
        // 提前调 logic() 会抛 "not initialized"（曾导致 enterprise 服务启动失败）。
        registry.addInterceptor(new SaInterceptor(handle -> logicFor(type).checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }

    /** 按 type 返回对应账号体系的 StpLogic；在请求处理期调用，此时绑定已完成。 */
    private static StpLogic logicFor(String type) {
        return switch (type.toLowerCase()) {
            case "admin" -> StpAdminUtil.logic();
            case "enterprise" -> StpEnterpriseUtil.logic();
            default -> StpUserUtil.logic();
        };
    }
}
