package com.chatcut.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建/更新角色请求")
public class RoleSaveReq {

    @Schema(description = "角色标识，全局唯一，创建后不建议修改", example = "content_editor", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 64)
    private String roleKey;

    @Schema(description = "角色名称", example = "内容编辑", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 64)
    private String roleName;

    @Schema(description = "角色描述", example = "只读内容管理权限")
    @Size(max = 255)
    private String description;

    @Schema(description = "该角色拥有的权限 id 集合，空数组表示清空全部权限", example = "[10, 11]")
    private List<Long> permissionIds;
}
