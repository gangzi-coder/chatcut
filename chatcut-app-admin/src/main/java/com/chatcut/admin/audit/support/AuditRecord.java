package com.chatcut.admin.audit.support;

import com.chatcut.common.core.trace.TraceContext;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 待落库的一条审计记录。切面与登录埋点各自组装，{@code AuditLogService} 负责写库。
 */
@Data
public class AuditRecord {

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

    private String content;
    private String beforeData;
    private String afterData;
    private String changedFields;
    private String detail;

    private String requestUri;
    private String httpMethod;
    private String requestData;

    private Integer result;
    private Integer errorCode;
    private String errorMsg;
    private Integer costMs;
    private LocalDateTime opTime;

    /** 字段级变更，落 sys_audit_log_change。 */
    private List<ChangeDraft> changes = new ArrayList<>();

    /**
     * 从当前请求补齐来源信息（traceId / IP / UA / 调用方 / URI / 方法）。
     *
     * <p>放在这里而不是 service：取值依赖 {@link AuditRequestInfo}，那是本包的实现细节，
     * 不该为了让 service 调到而放开成 public。</p>
     */
    public void fillRequestContext() {
        this.traceId = TraceContext.get();
        this.operatorIp = AuditRequestInfo.ip();
        this.operatorUa = AuditRequestInfo.userAgent();
        this.caller = AuditRequestInfo.caller();
        this.requestUri = AuditRequestInfo.uri();
        this.httpMethod = AuditRequestInfo.httpMethod();
    }
}
