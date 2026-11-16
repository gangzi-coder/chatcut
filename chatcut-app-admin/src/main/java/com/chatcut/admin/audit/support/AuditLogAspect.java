package com.chatcut.admin.audit.support;

import com.chatcut.admin.audit.annotation.AuditAction;
import com.chatcut.admin.audit.annotation.AuditLog;
import com.chatcut.admin.audit.service.AuditLogService;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.core.trace.TraceContext;
import com.chatcut.common.security.web.CurrentUser;
import com.chatcut.common.web.log.SensitiveFieldModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审计切面：拦截 {@link AuditLog} 标注的 controller 方法。
 *
 * <p>只负责「能自动拿到的东西」——操作者、IP/UA、调用方、入参、结果、耗时；
 * 操作对象、摘要、前后快照由业务在 service 里通过 {@link AuditContext} 补充。
 * 这样切面不需要理解任何业务语义，也不会因为业务字段变化而改。</p>
 *
 * <p>审计写入失败只打 ERROR 日志（含 traceId，可回捞），绝不影响业务返回。</p>
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    /** 入参里这些类型不参与序列化：流、会话、校验结果进 JSON 没意义还可能炸栈。 */
    private static final Set<Class<?>> SKIP_ARG_TYPES = Set.of(
            HttpSession.class, MultipartFile.class, BindingResult.class);

    /** 入参脱敏黑名单：字段名（小写）命中即输出 ***。 */
    private static final Set<String> MASKED_FIELDS = Set.of(
            "password", "oldpassword", "newpassword", "confirmpassword",
            "token", "authorization", "secret");

    private final AuditLogService auditLogService;

    /** 审计专用 mapper：在全局 mapper 基础上叠加字段名脱敏，不影响业务序列化。 */
    private final ObjectMapper auditMapper;

    public AuditLogAspect(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.auditMapper = objectMapper.copy().registerModule(new SensitiveFieldModule(MASKED_FIELDS));
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        AuditContext.begin();
        long startNanos = System.nanoTime();
        Throwable error = null;
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long costMs = (System.nanoTime() - startNanos) / 1_000_000;
            try {
                auditLogService.save(build(auditLog.value(), pjp, error, costMs));
            } catch (Exception e) {
                // 审计失败不能影响业务，降级为日志（带 traceId，可从日志回捞补齐）
                log.error("【审计-写入失败】-【动作: {}】-【请求: {}】-【原因: {}】",
                        auditLog.value().getCode(), AuditRequestInfo.uri(), e.toString());
            } finally {
                AuditContext.clear();
            }
        }
    }

    private AuditRecord build(AuditAction action, ProceedingJoinPoint pjp, Throwable error, long costMs) {
        AuditPayload payload = AuditContext.current();
        if (payload == null) {
            payload = new AuditPayload();
        }

        AuditRecord record = new AuditRecord();
        record.setTraceId(TraceContext.get());
        record.setModule(action.getModule());
        record.setAction(action.getCode());
        record.setActionName(action.getName());

        String targetType = StringUtils.hasText(payload.getTargetType()) ? payload.getTargetType() : action.getTargetType();
        record.setTargetType(targetType);
        record.setTargetId(payload.getTargetId());
        record.setTargetName(payload.getTargetName());

        // 操作者：业务可显式指定（登录等无会话场景），否则取当前登录管理员
        record.setOperatorId(payload.getOperatorId() != null ? payload.getOperatorId() : CurrentUser.adminId());
        record.setOperatorName(payload.getOperatorName());
        record.setOperatorIp(AuditRequestInfo.ip());
        record.setOperatorUa(AuditRequestInfo.userAgent());
        record.setCaller(AuditRequestInfo.caller());

        record.setContent(StringUtils.hasText(payload.getContent()) ? payload.getContent() : defaultContent(action, payload));

        record.setBeforeData(toJson(payload.getBefore()));
        record.setAfterData(toJson(payload.getAfter()));
        record.setDetail(payload.getDetail().isEmpty() ? null : toJson(payload.getDetail()));
        record.setChanges(payload.getChanges());
        String changedFields = payload.getChanges().stream()
                .map(ChangeDraft::getFieldName)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream().collect(Collectors.joining(","));
        record.setChangedFields(StringUtils.hasText(changedFields) ? changedFields : null);

        record.setRequestUri(AuditRequestInfo.uri());
        record.setHttpMethod(AuditRequestInfo.httpMethod());
        record.setRequestData(serializeArgs(pjp));

        applyResult(record, error);
        record.setCostMs((int) costMs);
        record.setOpTime(LocalDateTime.now());
        return record;
    }

    /** 摘要兜底：动作名 + 对象标识。对象名拿不到（如校验阶段就失败）时退回 ID，至少能定位到人。 */
    private String defaultContent(AuditAction action, AuditPayload payload) {
        if (StringUtils.hasText(payload.getTargetName())) {
            return action.getName() + " " + payload.getTargetName();
        }
        if (StringUtils.hasText(payload.getTargetId())) {
            return action.getName() + " ID:" + payload.getTargetId();
        }
        return action.getName();
    }

    private void applyResult(AuditRecord record, Throwable error) {
        if (error == null) {
            record.setResult(AuditResult.SUCCESS.getCode());
            return;
        }
        if (error instanceof BizException biz) {
            record.setResult(AuditResult.REJECTED.getCode());
            record.setErrorCode(biz.getCode());
            record.setErrorMsg(biz.getMessage());
            return;
        }
        record.setResult(AuditResult.ERROR.getCode());
        record.setErrorCode(CommonErrorCode.SYSTEM_ERROR.code());
        String message = error.getMessage();
        record.setErrorMsg(error.getClass().getSimpleName() + (message == null ? "" : ": " + message));
    }

    /** 入参快照：参数名 -> 值，密码类字段由 auditMapper 的脱敏模块输出 ***。 */
    private String serializeArgs(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] names = signature.getParameterNames();
            Object[] args = pjp.getArgs();
            Map<String, Object> params = new LinkedHashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse
                        || SKIP_ARG_TYPES.stream().anyMatch(type -> type.isInstance(arg))) {
                    continue;
                }
                String name = names != null && i < names.length ? names[i] : "arg" + i;
                params.put(name, arg);
            }
            return params.isEmpty() ? null : auditMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{\"_serializeError\":\"" + e.getClass().getSimpleName() + "\"}";
        }
    }

    private String toJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return auditMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
