package com.chatcut.admin.audit.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 审计变更明细 {@code sys_audit_log_change}：一行一个字段变更。
 */
@Data
@TableName("sys_audit_log_change")
public class SysAuditLogChangeEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联 sys_audit_log.id。 */
    private Long auditId;

    private String fieldName;
    private String fieldLabel;

    /** 原始值，供精确匹配/反查（如按权限 id 查）。 */
    private String oldValue;
    private String newValue;

    /** 展示值，字典已转义（如「启用」「停用」）。 */
    private String oldDisplay;
    private String newDisplay;

    /** UPDATE / ADD / REMOVE。 */
    private String changedType;
}
