package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 角色信息（对外传输，不含 DO 字段如 deleted/version）。 */
@Data
@Schema(description = "角色信息")
public class RoleResp {

    @Schema(description = "角色 id", example = "1")
    private Long id;

    @Schema(description = "角色标识（唯一，代码里 @SaCheckPermission 的角色判断用它）", example = "super_admin")
    private String roleKey;

    @Schema(description = "角色名称（展示用）", example = "超级管理员")
    private String roleName;

    @Schema(description = "角色描述", example = "拥有全部权限")
    private String description;

    @Schema(description = "状态：1=启用 0=停用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
