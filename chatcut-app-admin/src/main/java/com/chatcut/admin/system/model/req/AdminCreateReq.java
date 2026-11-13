package com.chatcut.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建管理员请求")
public class AdminCreateReq {

    @Schema(description = "登录用户名，全局唯一", example = "ops01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @Schema(description = "初始密码，12~128 位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 12, max = 128)
    private String password;

    @Schema(description = "显示名称", example = "运维同学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 64)
    private String displayName;

    @Schema(description = "关联角色 id 列表，缺省为不授权", example = "[2]")
    private List<Long> roleIds;
}
