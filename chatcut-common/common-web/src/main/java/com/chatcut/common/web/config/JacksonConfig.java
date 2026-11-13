package com.chatcut.common.web.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 JSON 约定：
 * 1) Long/long 序列化成字符串，避免前端 JS 精度丢失（雪花 ID 19 位必丢）；
 * 2) 时间统一 yyyy-MM-dd HH:mm:ss（后端内部仍用 UTC 存储）。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer chatcutJacksonCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
        };
    }
}
