package com.chatcut.common.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流。key 为限流维度名，identity 默认取当前用户（无则取 IP）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String name();

    /** 时间窗口内允许的次数 */
    int permits() default 30;

    /** 窗口秒数 */
    int windowSeconds() default 60;

    /** 限流维度：USER / IP */
    String dimension() default "USER";
}
