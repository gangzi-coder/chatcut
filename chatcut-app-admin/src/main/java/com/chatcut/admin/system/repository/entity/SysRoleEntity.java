package com.chatcut.admin.system.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chatcut.admin.audit.annotation.AuditDisplay;
import com.chatcut.admin.audit.annotation.AuditField;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.mybatis.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色，对应表 {@code sys_role}。
 *
 * <p>角色通过 {@code sys_role_permission} 关联权限项；roleKey 供代码里做角色判断。
 * 审计快照只取 {@link AuditField} 标注的字段。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity {

    /** 角色标识，全局唯一（数据库 UNIQUE KEY），创建后不建议修改。 */
    @AuditField(label = "角色标识")
    private String roleKey;

    /** 角色名称，展示用。 */
    @AuditField(label = "角色名称")
    private String roleName;

    /** 角色描述，可空。 */
    @AuditField(label = "角色描述")
    private String description;

    /**
     * 角色状态：0=停用 1=启用，停用后其权限不参与鉴权。
     *
     * @see EnableStatus 取值枚举
     */
    @AuditField(label = "状态", display = AuditDisplay.ENABLE_STATUS)
    private Integer status;
}
