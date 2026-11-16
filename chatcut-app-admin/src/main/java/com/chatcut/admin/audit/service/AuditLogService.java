package com.chatcut.admin.audit.service;

import com.chatcut.admin.audit.annotation.AuditAction;
import com.chatcut.admin.audit.repository.entity.SysAuditLogChangeEntity;
import com.chatcut.admin.audit.repository.entity.SysAuditLogEntity;
import com.chatcut.admin.audit.repository.mapper.SysAuditLogChangeMapper;
import com.chatcut.admin.audit.repository.mapper.SysAuditLogMapper;
import com.chatcut.admin.audit.support.AuditRecord;
import com.chatcut.admin.audit.support.AuditResult;
import com.chatcut.admin.audit.support.ChangeDraft;
import com.chatcut.admin.system.repository.entity.SysAdminEntity;
import com.chatcut.admin.system.repository.mapper.AdminMapper;
import com.chatcut.common.core.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 审计落库。
 *
 * <p>三条硬约束：</p>
 * <ol>
 *   <li><b>独立事务（REQUIRES_NEW）</b>：业务回滚时审计照记——审计要证明的是「发生过这次尝试」，
 *       而不是「业务最终成功」；反过来审计写失败也绝不能把业务拖回滚，由调用方兜底打日志。</li>
 *   <li><b>只 INSERT</b>：本类不提供任何更新/删除入口。</li>
 *   <li><b>不抛异常给业务</b>：异常在切面/埋点处被捕获并降级为 ERROR 日志（带 traceId，可回捞）。</li>
 * </ol>
 */
@Slf4j
@Service
public class AuditLogService {

    private final SysAuditLogMapper auditLogMapper;
    private final SysAuditLogChangeMapper changeMapper;
    private final AdminMapper adminMapper;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    public AuditLogService(SysAuditLogMapper auditLogMapper,
                           SysAuditLogChangeMapper changeMapper,
                           AdminMapper adminMapper,
                           com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.changeMapper = changeMapper;
        this.adminMapper = adminMapper;
        this.objectMapper = objectMapper;
    }

    /** 写一条审计（主表 + 明细，同一新事务）。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void save(AuditRecord record) {
        doSave(record);
    }

    /**
     * 登录留痕。登录失败时还没有会话，操作者只能取「尝试登录的用户名」，
     * 这正是排查暴力破解最需要的信息。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordLogin(String username, boolean success, String message, Integer errorCode) {
        AuditRecord record = new AuditRecord();
        record.fillRequestContext();
        record.setAction(AuditAction.LOGIN.getCode());
        record.setActionName(AuditAction.LOGIN.getName());
        record.setModule(AuditAction.LOGIN.getModule());
        record.setOperatorName(username);
        record.setContent(success ? "登录成功" : "登录失败：" + message);
        record.setResult(success ? AuditResult.SUCCESS.getCode() : AuditResult.REJECTED.getCode());
        record.setErrorCode(errorCode);
        record.setErrorMsg(success ? null : message);
        record.setCostMs(0);
        record.setOpTime(LocalDateTime.now());
        doSave(record);
    }

    /** 登出留痕：此时会话还在，能取到操作者。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordLogout(Long adminId, String username) {
        AuditRecord record = new AuditRecord();
        record.fillRequestContext();
        record.setAction(AuditAction.LOGOUT.getCode());
        record.setActionName(AuditAction.LOGOUT.getName());
        record.setModule(AuditAction.LOGOUT.getModule());
        record.setOperatorId(adminId);
        record.setOperatorName(username);
        record.setContent("登出");
        record.setResult(AuditResult.SUCCESS.getCode());
        record.setCostMs(0);
        record.setOpTime(LocalDateTime.now());
        doSave(record);
    }

    private void doSave(AuditRecord record) {
        if (record.getOpTime() == null) {
            record.setOpTime(LocalDateTime.now());
        }
        if (!StringUtils.hasText(record.getTraceId())) {
            record.setTraceId(TraceContext.get());
        }
        if (!StringUtils.hasText(record.getOperatorName()) && record.getOperatorId() != null) {
            record.setOperatorName(resolveOperatorName(record.getOperatorId()));
        }

        SysAuditLogEntity entity = toEntity(record);
        auditLogMapper.insert(entity);

        List<ChangeDraft> changes = record.getChanges();
        if (changes != null && !changes.isEmpty()) {
            for (ChangeDraft draft : changes) {
                SysAuditLogChangeEntity change = new SysAuditLogChangeEntity();
                change.setAuditId(entity.getId());
                change.setFieldName(draft.getFieldName());
                change.setFieldLabel(draft.getFieldLabel());
                change.setOldValue(draft.getOldValue());
                change.setNewValue(draft.getNewValue());
                change.setOldDisplay(draft.getOldDisplay());
                change.setNewDisplay(draft.getNewDisplay());
                change.setChangedType(draft.getChangedType());
                changeMapper.insert(change);
            }
        }
        log.info("【审计-记录】-【动作: {}】-【对象: {}:{}】-【操作者: {}】-【结果: {}】-【内容: {}】",
                record.getAction(), record.getTargetType(), record.getTargetId(),
                record.getOperatorName(), record.getResult(), record.getContent());
    }

    private SysAuditLogEntity toEntity(AuditRecord record) {
        SysAuditLogEntity entity = new SysAuditLogEntity();
        entity.setTraceId(Objects.toString(record.getTraceId(), "-"));
        entity.setOperatorId(record.getOperatorId());
        entity.setOperatorName(record.getOperatorName());
        entity.setOperatorIp(Objects.toString(record.getOperatorIp(), "-"));
        entity.setOperatorUa(record.getOperatorUa());
        entity.setCaller(Objects.toString(record.getCaller(), "direct"));
        entity.setModule(record.getModule());
        entity.setAction(record.getAction());
        entity.setActionName(record.getActionName());
        entity.setTargetType(record.getTargetType());
        entity.setTargetId(record.getTargetId());
        entity.setTargetName(record.getTargetName());
        entity.setContent(Objects.toString(record.getContent(), record.getActionName()));
        entity.setBeforeData(record.getBeforeData());
        entity.setAfterData(record.getAfterData());
        entity.setChangedFields(record.getChangedFields());
        entity.setDetail(record.getDetail());
        entity.setRequestUri(Objects.toString(record.getRequestUri(), "-"));
        entity.setHttpMethod(Objects.toString(record.getHttpMethod(), "-"));
        entity.setRequestData(record.getRequestData());
        entity.setResult(record.getResult());
        entity.setErrorCode(record.getErrorCode());
        entity.setErrorMsg(truncate(record.getErrorMsg(), 500));
        entity.setCostMs(record.getCostMs() == null ? 0 : record.getCostMs());
        entity.setOpTime(record.getOpTime());
        return entity;
    }

    private String resolveOperatorName(Long adminId) {
        try {
            SysAdminEntity admin = adminMapper.selectById(adminId);
            return admin == null ? "ID:" + adminId : admin.getUsername();
        } catch (Exception e) {
            // 查人失败不该让审计写不进去，ID 本身已足够定位
            return "ID:" + adminId;
        }
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
