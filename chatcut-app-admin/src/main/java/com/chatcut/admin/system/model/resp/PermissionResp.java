package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 权限项信息（对外传输，不含 DO 字段如 deleted/version）。 */
@Data
@Schema(description = "权限项信息")
public class PermissionResp {

    @Schema(description = "权限 id", example = "10")
    private Long id;

    @Schema(description = "父权限 id，顶级为 null（预留菜单树用）")
    private Long parentId;

    @Schema(description = "权限标识，与 @SaCheckPermission 的值一致", example = "system:role:create")
    private String permissionKey;

    @Schema(description = "权限名称（展示用）", example = "角色管理")
    private String permissionName;

    @Schema(description = "权限类型：menu=菜单 button=按钮 api=接口", example = "button")
    private String permissionType;

    @Schema(description = "关联前端路由路径，接口型权限可为空")
    private String path;

    @Schema(description = "HTTP 方法，接口型权限用", example = "POST")
    private String httpMethod;

    @Schema(description = "排序号，越小越靠前", example = "1")
    private Integer sort;

    @Schema(description = "状态：1=启用 0=停用", example = "1")
    private Integer status;
}
