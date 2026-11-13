package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "当前登录管理员信息")
public class AdminProfileResp {

    @Schema(description = "管理员 id", example = "1")
    private Long id;

    @Schema(description = "登录用户名", example = "admin")
    private String username;

    @Schema(description = "显示名称", example = "系统管理员")
    private String displayName;

    @Schema(description = "角色 key 列表", example = "[\"super_admin\"]")
    private List<String> roles;

    @Schema(description = "权限 key 列表，前端据此控制按钮显隐", example = "[\"system:role:create\"]")
    private List<String> permissions;
}
