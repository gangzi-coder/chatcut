package com.chatcut.admin.system.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.mybatis.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限项，对应表 {@code sys_permission}。
 *
 * <p>permissionKey 与代码里 {@code @SaCheckPermission} 的值一致；
 * parentId 预留菜单树，当前全部为顶级。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermissionEntity extends BaseEntity {

    /** 父权限 id，顶级为 null（预留菜单树用）。 */
    private Long parentId;

    /** 权限标识，格式 {@code 模块:资源:操作}，如 system:role:create。 */
    private String permissionKey;

    /** 权限名称，展示用。 */
    private String permissionName;

    /** 权限类型：menu=菜单 button=按钮 api=接口。 */
    private String permissionType;

    /** 关联前端路由路径，接口型权限可为空。 */
    private String path;

    /** HTTP 方法，接口型权限用（GET/POST）。 */
    private String httpMethod;

    /** 排序号，越小越靠前。 */
    private Integer sort;

    /**
     * 权限状态：0=停用 1=启用，停用后不参与鉴权与角色绑定校验。
     *
     * @see EnableStatus 取值枚举
     */
    private Integer status;
}
