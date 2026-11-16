package com.chatcut.admin.audit.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 字段级变更明细。
 */
@Data
@Schema(description = "审计变更明细")
public class AuditChangeResp {

    @Schema(description = "字段名")
    private String fieldName;

    @Schema(description = "字段中文名")
    private String fieldLabel;

    @Schema(description = "变更前原始值")
    private String oldValue;

    @Schema(description = "变更后原始值")
    private String newValue;

    @Schema(description = "变更前展示值，字典已转义")
    private String oldDisplay;

    @Schema(description = "变更后展示值")
    private String newDisplay;

    @Schema(description = "变更类型：UPDATE/ADD/REMOVE")
    private String changedType;
}
