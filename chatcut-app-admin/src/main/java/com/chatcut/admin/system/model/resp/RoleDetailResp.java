package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/** 角色详情：含该角色已勾选的权限 id 列表（前端回显用）。 */
@Data
@Schema(description = "角色详情（编辑回显）")
public class RoleDetailResp {

    @Schema(description = "角色 id", example = "1")
    private Long id;

    @Schema(description = "角色标识", example = "super_admin")
    private String roleKey;

    @Schema(description = "角色名称", example = "超级管理员")
    private String roleName;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "状态：1=启用 0=停用", example = "1")
    private Integer status;

    @Schema(description = "已勾选的权限 id 列表，编辑角色弹窗回显用")
    private List<Long> permissionIds;
}
