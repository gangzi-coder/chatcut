package com.chatcut.enterprise.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置（springdoc-openapi）。
 * 联调入口（本服务 9301）：
 * <ul>
 *   <li>knife4j 增强页: http://127.0.0.1:9301/doc.html</li>
 *   <li>Swagger UI: http://127.0.0.1:9301/swagger-ui/index.html</li>
 *   <li>OpenAPI JSON: http://127.0.0.1:9301/v3/api-docs</li>
 * </ul>
 * 先调 /auth/login 拿 tokenValue，再点右上角 Authorize 输入 token（自动加 Bearer 前缀）。
 * 经网关访问时前缀为 /enterprise/**（网关 StripPrefix 后转发到本服务）。
 * 测试/生产环境通过 Nacos 配置把 springdoc.api-docs.enabled / swagger-ui.enabled 关成 false。
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "Authorization";

    @Bean
    public OpenAPI enterpriseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatCut Enterprise API")
                        .description("ChatCut B 端接口：企业认证、组织/团队、成员、项目协作。统一响应 Result{code,msg,data,traceId}，code=0 成功。")
                        .version("1.0.0"))
                // HTTP bearer：UI 里只填 token 值，自动发送 Authorization: Bearer <token>，与 sa-token 配置一致
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .description("B 端登录接口返回的 tokenValue（enterprise 体系）")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME));
    }
}
