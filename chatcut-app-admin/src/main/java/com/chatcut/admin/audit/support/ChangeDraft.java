package com.chatcut.admin.audit.support;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一条字段级变更草稿，落库时写进 sys_audit_log_change。
 *
 * <p>raw 值用于反查（如按权限 id 查），display 值用于人读（如「启用 → 停用」）。</p>
 */
@Data
@AllArgsConstructor
public class ChangeDraft {

    /** 字段名：status / roleName / permissions。 */
    private String fieldName;

    /** 字段中文名：状态 / 角色名称 / 权限。 */
    private String fieldLabel;

    /** 变更前原始值。 */
    private String oldValue;

    /** 变更后原始值。 */
    private String newValue;

    /** 变更前展示值，字典已转义。 */
    private String oldDisplay;

    /** 变更后展示值。 */
    private String newDisplay;

    /** UPDATE / ADD / REMOVE。 */
    private String changedType;

    public static ChangeDraft update(String field, String label, String oldValue, String newValue,
                                     String oldDisplay, String newDisplay) {
        return new ChangeDraft(field, label, oldValue, newValue, oldDisplay, newDisplay, "UPDATE");
    }
}
