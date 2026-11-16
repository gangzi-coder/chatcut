/**
 * 应用级装配：本服务的 Bean 定义与框架配置，不属于任何业务域。
 *
 * <p>放这里的判据是「脱离业务域它还成立」——Swagger 文档、密码编码器、全局序列化、Web 拦截器这类。
 * 反过来，绑死某个域的（例如某张表的 Mapper 配置）就留在域内。
 * 业务域包（{@code system} / {@code content} / {@code audit}）内不放 {@code @Configuration}。</p>
 *
 * <p>同一份配置如果在两个以上服务里要复制，再下沉到 {@code chatcut-common} 做成自动配置 + 属性开关，
 * 而不是服务之间互相依赖。</p>
 */
package com.chatcut.admin.config;
