package com.chatcut.admin.system.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 角色简要信息（用于管理员列表/详情内嵌展示）。 */
@Data
@Schema(description = "角色简要信息（管理员列表/详情内嵌）")
public class RoleBriefResp {

    @Schema(description = "角色 id", example = "1")
    private Long id;

    @Schema(description = "角色标识", example = "super_admin")
    private String roleKey;

    @Schema(description = "角色名称", example = "超级管理员")
    private String roleName;
}
