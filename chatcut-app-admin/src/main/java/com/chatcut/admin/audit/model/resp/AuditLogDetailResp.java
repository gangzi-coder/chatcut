package com.chatcut.admin.audit.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计详情：列表字段 + 请求入参 + 前后快照 + 字段级变更。
 *
 * <p>快照以 JSON 字符串返回，前端负责格式化展示；后端不做二次解析，
 * 避免历史数据格式演进时接口跟着改。</p>
 */
@Data
@Schema(description = "审计日志详情")
public class AuditLogDetailResp {

    private Long id;

    private LocalDateTime opTime;
    private Long operatorId;
    private String operatorName;
    private String operatorIp;
    private String operatorUa;
    private String caller;

    private String module;
    private String action;
    private String actionName;

    private String targetType;
    private String targetId;
    private String targetName;

    private String content;

    @Schema(description = "变更前快照（JSON 字符串）")
    private String beforeData;

    @Schema(description = "变更后快照（JSON 字符串）")
    private String afterData;

    @Schema(description = "变更字段清单，逗号分隔")
    private String changedFields;

    @Schema(description = "补充明细（JSON 字符串），如权限增删清单")
    private String detail;

    private String requestUri;
    private String httpMethod;

    @Schema(description = "入参快照（JSON 字符串，密码类字段已脱敏）")
    private String requestData;

    private Integer result;
    private Integer errorCode;
    private String errorMsg;
    private Integer costMs;
    private String traceId;

    @Schema(description = "字段级变更明细")
    private List<AuditChangeResp> changes;
}
