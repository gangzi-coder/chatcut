package com.chatcut.admin.audit.model.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计列表行：只给列表页需要的字段，快照和明细留给详情接口。
 */
@Data
@Schema(description = "审计日志列表项")
public class AuditLogResp {

    private Long id;

    @Schema(description = "操作时间")
    private LocalDateTime opTime;

    @Schema(description = "操作者 ID；登录失败时为空")
    private Long operatorId;

    @Schema(description = "操作者用户名")
    private String operatorName;

    @Schema(description = "操作者 IP")
    private String operatorIp;

    @Schema(description = "调用方：chatcut-gateway / direct")
    private String caller;

    @Schema(description = "模块")
    private String module;

    @Schema(description = "动作码")
    private String action;

    @Schema(description = "动作名")
    private String actionName;

    @Schema(description = "对象类型")
    private String targetType;

    @Schema(description = "对象 ID")
    private String targetId;

    @Schema(description = "对象名称快照")
    private String targetName;

    @Schema(description = "操作摘要")
    private String content;

    @Schema(description = "结果：1成功 2业务拒绝 3系统异常")
    private Integer result;

    @Schema(description = "失败错误码")
    private Integer errorCode;

    @Schema(description = "失败原因")
    private String errorMsg;

    @Schema(description = "耗时（毫秒）")
    private Integer costMs;

    @Schema(description = "链路 ID")
    private String traceId;
}
