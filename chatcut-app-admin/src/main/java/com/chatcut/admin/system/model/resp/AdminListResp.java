package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 管理员列表 / 详情条目。 */
@Data
@Schema(description = "管理员信息（列表/详情）")
public class AdminListResp {

    @Schema(description = "管理员 id", example = "1")
    private Long id;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "显示名称", example = "系统管理员")
    private String displayName;

    @Schema(description = "手机号，可为空", example = "13800000000")
    private String phone;

    @Schema(description = "状态：1=启用 0=停用", example = "1")
    private Integer status;

    @Schema(description = "最近登录时间，从未登录为 null")
    private LocalDateTime lastLoginAt;

    @Schema(description = "拥有的角色列表")
    private List<RoleBriefResp> roles;
}
