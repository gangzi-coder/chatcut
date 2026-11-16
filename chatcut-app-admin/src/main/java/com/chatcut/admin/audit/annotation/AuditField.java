package com.chatcut.admin.audit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记「需要进审计快照」的字段——<b>白名单机制</b>。
 *
 * <p>只有加了本注解的字段才会写进 {@code before_data} / {@code after_data} 并参与 diff。
 * 密码哈希、盐、令牌这类字段不加注解，就永远不会出现在审计里，不依赖任何黑名单维护。</p>
 *
 * <pre>
 * &#64;AuditField(label = "状态", display = AuditDisplay.ENABLE_STATUS)
 * private Integer status;
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuditField {

    /** 字段中文名，写进变更明细的 field_label。 */
    String label();

    /** 展示值转换方式。 */
    AuditDisplay display() default AuditDisplay.RAW;
}
