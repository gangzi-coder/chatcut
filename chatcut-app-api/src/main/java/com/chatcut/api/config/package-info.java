/**
 * 服务级装配，脱离业务域还成立的配置放这里：Swagger 文档、全局序列化、CORS 等。
 * 业务域包内不放 {@code @Configuration}。
 * 同一份配置如果需要复制到两个以上服务，下沉 chatcut-common 做自动配置 + 属性开关，
 * 而不是让服务之间互相依赖。
 */
package com.chatcut.api.config;
