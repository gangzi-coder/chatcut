package com.chatcut.api.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置（springdoc-openapi）。
 * 联调入口：
 *   - knife4j 增强页: http://127.0.0.1:8081/doc.html
 *   - Swagger UI:     http://127.0.0.1:8081/swagger-ui/index.html
 *   - OpenAPI JSON:   http://127.0.0.1:8081/v3/api-docs
 * 先调 /auth/login 拿 tokenValue，再点右上角 Authorize 输入 token（自动加 Bearer 前缀）。
 * 测试/生产环境通过 Nacos 配置把 springdoc.api-docs.enabled / swagger-ui.enabled 关成 false。
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "Authorization";

    @Bean
    public OpenAPI apiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatCut App API")
                        .description("ChatCut C 端接口：认证、用户、剪辑、AI、文件、任务。统一响应 Result{code,msg,data,traceId}，code=0 成功。")
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
