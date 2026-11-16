package com.chatcut.admin.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要留痕的 controller 方法。切面负责采集操作者、IP、入参、结果、耗时；
 * <b>操作对象、操作摘要和前后快照由业务在 service 里通过
 * {@link com.chatcut.admin.audit.support.AuditContext} 补充</b>。
 *
 * <pre>
 * &#64;AuditLog(AuditAction.ADMIN_STATUS)
 * &#64;PostMapping("/{id}/status")
 * public Result&lt;Void&gt; updateStatus(...)
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {

    /** 动作，决定模块、动作码、动作名与对象类型。 */
    AuditAction value();
}
