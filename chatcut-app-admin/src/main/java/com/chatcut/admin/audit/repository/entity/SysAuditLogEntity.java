package com.chatcut.admin.audit.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chatcut.admin.audit.support.AuditResult;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计主表 {@code sys_audit_log}。
 *
 * <p>刻意<b>不继承</b> BaseEntity：审计表没有 deleted / version / updated_at，
 * 也不允许 update——表里没有这些列，实体里也就不该有这些字段。</p>
 */
@Data
@TableName("sys_audit_log")
public class SysAuditLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 链路 ID，与业务日志的 MDC traceId 一致。 */
    private String traceId;

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

    /** 操作摘要，列表页直接展示。 */
    private String content;

    /** JSON 字符串，MySQL 侧为 JSON 列。 */
    private String beforeData;
    private String afterData;
    private String changedFields;
    private String detail;

    private String requestUri;
    private String httpMethod;
    private String requestData;

    /** 1成功 2业务拒绝 3系统异常，见 AuditResult。 */
    private Integer result;
    private Integer errorCode;
    private String errorMsg;
    private Integer costMs;
    private LocalDateTime opTime;
}
