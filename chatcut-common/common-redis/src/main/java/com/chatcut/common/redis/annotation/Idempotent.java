package com.chatcut.common.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等保护：同一用户 + 同一方法 + 相同参数，在 ttl 秒内只放行一次。
 * 只用于"创建/提交"类接口，查询接口不要加。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    String name();

    int ttlSeconds() default 10;
}
