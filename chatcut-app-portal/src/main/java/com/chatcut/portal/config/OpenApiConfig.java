package com.chatcut.portal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 配置（springdoc-openapi）。
 * 联调入口（本服务 9401）：
 * <ul>
 *   <li>knife4j 增强页: http://127.0.0.1:9401/doc.html</li>
 *   <li>Swagger UI: http://127.0.0.1:9401/swagger-ui/index.html</li>
 *   <li>OpenAPI JSON: http://127.0.0.1:9401/v3/api-docs</li>
 * </ul>
 * 本服务无登录态，接口全部匿名可访问，无需 Authorize。
 * 经网关访问时前缀为 /portal/**。测试/生产环境通过 Nacos 关闭文档。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ChatCut Portal API")
                        .description("ChatCut 官网门户接口：免登录只读，查询后台配置的官网展示数据。统一响应 Result{code,msg,data,traceId}，code=0 成功。")
                        .version("1.0.0"));
    }
}
