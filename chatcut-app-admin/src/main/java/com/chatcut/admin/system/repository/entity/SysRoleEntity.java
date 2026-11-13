package com.chatcut.admin.system.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.mybatis.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色，对应表 {@code sys_role}。
 *
 * <p>角色通过 {@code sys_role_permission} 关联权限项；roleKey 供代码里做角色判断。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity {

    /** 角色标识，全局唯一（数据库 UNIQUE KEY），创建后不建议修改。 */
    private String roleKey;

    /** 角色名称，展示用。 */
    private String roleName;

    /** 角色描述，可空。 */
    private String description;

    /**
     * 角色状态：0=停用 1=启用，停用后其权限不参与鉴权。
     *
     * @see EnableStatus 取值枚举
     */
    private Integer status;
}
