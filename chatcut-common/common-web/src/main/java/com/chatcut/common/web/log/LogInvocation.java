package com.chatcut.common.web.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 service 层方法打印出入参（controller 层按包路径自动切入，无需此注解）。
 * 只在"这个方法的输入输出对排查很重要"时使用，避免日志噪音。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogInvocation {
}
