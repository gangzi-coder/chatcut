package com.chatcut.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置（springdoc-openapi），服务级，与业务域无关。
 *
 * <p>联调入口（本机 9101 端口）：</p>
 * <ul>
 *   <li>knife4j 增强页：{@code http://127.0.0.1:9101/doc.html}</li>
 *   <li>Swagger UI：{@code http://127.0.0.1:9101/swagger-ui/index.html}</li>
 *   <li>OpenAPI JSON：{@code http://127.0.0.1:9101/v3/api-docs}</li>
 * </ul>
 * <p>先调 {@code /auth/login} 拿 tokenValue，再点右上角 Authorize 输入 token（自动加 Bearer 前缀）。
 * 测试/生产环境通过 Nacos 配置把 {@code springdoc.api-docs.enabled} / {@code swagger-ui.enabled} 关成 false。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "Authorization";

    @Bean
    public OpenAPI adminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatCut Admin API")
                        .description("ChatCut 管理端接口：认证、管理员、角色与权限。统一响应 Result{code,msg,data,traceId}，code=0 成功。")
                        .version("1.0.0"))
                // HTTP bearer：UI 里只填 token 值，自动发送 Authorization: Bearer <token>，与 sa-token 配置一致
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("登录接口返回的 tokenValue")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }
}
