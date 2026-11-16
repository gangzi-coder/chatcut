package com.chatcut.admin.audit.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 审计日志查询条件。全部可选，时间范围按操作时间过滤。
 */
@Data
@Schema(description = "审计日志查询条件")
public class AuditLogQueryReq {

    @Schema(description = "页码，从 1 开始", example = "1")
    private long pageNo = 1;

    @Schema(description = "每页条数，上限 200", example = "20")
    private long pageSize = 20;

    @Schema(description = "操作者用户名，模糊匹配")
    private String operatorName;

    @Schema(description = "模块：管理员/角色/认证")
    private String module;

    @Schema(description = "动作码：ADMIN_CREATE/ROLE_UPDATE/LOGIN...")
    private String action;

    @Schema(description = "对象类型：admin/role")
    private String targetType;

    @Schema(description = "对象 ID，精确匹配")
    private String targetId;

    @Schema(description = "结果：1成功 2业务拒绝 3系统异常")
    private Integer result;

    @Schema(description = "链路 ID，精确匹配。网关透传下来的那个 traceId，排查「某次请求到底留痕没有」时用")
    private String traceId;

    @Schema(description = "调用方：chatcut-gateway 表示经网关，direct 表示直连服务")
    private String caller;

    @Schema(description = "操作时间起（含），格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "操作时间止（含），格式 yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
