package com.chatcut.admin.audit.annotation;

/**
 * 审计字段的展示值转换方式。
 *
 * <p>库里存原始值（0/1），展示列存转义后的值（停用/启用）。新增字典时在这里加一项，
 * 不要在 AuditDiff 里写 if-else。</p>
 */
public enum AuditDisplay {

    /** 原样输出。 */
    RAW,

    /** 通用启用状态：1=启用，0=停用（见 {@code EnableStatus}）。 */
    ENABLE_STATUS
}
