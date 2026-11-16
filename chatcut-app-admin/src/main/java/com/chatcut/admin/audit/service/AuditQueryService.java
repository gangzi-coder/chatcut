package com.chatcut.admin.audit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.audit.model.req.AuditLogQueryReq;
import com.chatcut.admin.audit.model.resp.AuditChangeResp;
import com.chatcut.admin.audit.model.resp.AuditLogDetailResp;
import com.chatcut.admin.audit.model.resp.AuditLogResp;
import com.chatcut.admin.audit.repository.entity.SysAuditLogChangeEntity;
import com.chatcut.admin.audit.repository.entity.SysAuditLogEntity;
import com.chatcut.admin.audit.repository.mapper.SysAuditLogChangeMapper;
import com.chatcut.admin.audit.repository.mapper.SysAuditLogMapper;
import com.chatcut.common.core.domain.PageResult;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 审计查询（只读）。没有写接口——审计记录不允许通过接口改动。
 */
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private static final long MAX_PAGE_SIZE = 200;

    private final SysAuditLogMapper auditLogMapper;
    private final SysAuditLogChangeMapper changeMapper;

    public PageResult<AuditLogResp> page(AuditLogQueryReq req) {
        long pageNo = Math.max(req.getPageNo(), 1);
        long pageSize = Math.min(Math.max(req.getPageSize(), 1), MAX_PAGE_SIZE);

        LambdaQueryWrapper<SysAuditLogEntity> query = Wrappers.<SysAuditLogEntity>lambdaQuery()
                .like(StringUtils.hasText(req.getOperatorName()), SysAuditLogEntity::getOperatorName, req.getOperatorName())
                .eq(StringUtils.hasText(req.getModule()), SysAuditLogEntity::getModule, req.getModule())
                .eq(StringUtils.hasText(req.getAction()), SysAuditLogEntity::getAction, req.getAction())
                .eq(StringUtils.hasText(req.getTargetType()), SysAuditLogEntity::getTargetType, req.getTargetType())
                .eq(StringUtils.hasText(req.getTargetId()), SysAuditLogEntity::getTargetId, req.getTargetId())
                .eq(req.getResult() != null, SysAuditLogEntity::getResult, req.getResult())
                .ge(req.getStartTime() != null, SysAuditLogEntity::getOpTime, req.getStartTime())
                .le(req.getEndTime() != null, SysAuditLogEntity::getOpTime, req.getEndTime())
                .orderByDesc(SysAuditLogEntity::getOpTime)
                .orderByDesc(SysAuditLogEntity::getId);

        Page<SysAuditLogEntity> page = auditLogMapper.selectPage(new Page<>(pageNo, pageSize), query);
        List<AuditLogResp> list = page.getRecords().stream().map(AuditQueryService::toResp).toList();
        return PageResult.of(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AuditLogDetailResp detail(Long id) {
        SysAuditLogEntity entity = auditLogMapper.selectById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "审计记录不存在");
        }
        AuditLogDetailResp resp = new AuditLogDetailResp();
        resp.setId(entity.getId());
        resp.setOpTime(entity.getOpTime());
        resp.setOperatorId(entity.getOperatorId());
        resp.setOperatorName(entity.getOperatorName());
        resp.setOperatorIp(entity.getOperatorIp());
        resp.setOperatorUa(entity.getOperatorUa());
        resp.setCaller(entity.getCaller());
        resp.setModule(entity.getModule());
        resp.setAction(entity.getAction());
        resp.setActionName(entity.getActionName());
        resp.setTargetType(entity.getTargetType());
        resp.setTargetId(entity.getTargetId());
        resp.setTargetName(entity.getTargetName());
        resp.setContent(entity.getContent());
        resp.setBeforeData(entity.getBeforeData());
        resp.setAfterData(entity.getAfterData());
        resp.setChangedFields(entity.getChangedFields());
        resp.setDetail(entity.getDetail());
        resp.setRequestUri(entity.getRequestUri());
        resp.setHttpMethod(entity.getHttpMethod());
        resp.setRequestData(entity.getRequestData());
        resp.setResult(entity.getResult());
        resp.setErrorCode(entity.getErrorCode());
        resp.setErrorMsg(entity.getErrorMsg());
        resp.setCostMs(entity.getCostMs());
        resp.setTraceId(entity.getTraceId());
        resp.setChanges(changeMapper.selectList(Wrappers.<SysAuditLogChangeEntity>lambdaQuery()
                        .eq(SysAuditLogChangeEntity::getAuditId, id)
                        .orderByAsc(SysAuditLogChangeEntity::getId))
                .stream().map(AuditQueryService::toResp).toList());
        return resp;
    }

    private static AuditLogResp toResp(SysAuditLogEntity e) {
        AuditLogResp vo = new AuditLogResp();
        vo.setId(e.getId());
        vo.setOpTime(e.getOpTime());
        vo.setOperatorId(e.getOperatorId());
        vo.setOperatorName(e.getOperatorName());
        vo.setOperatorIp(e.getOperatorIp());
        vo.setCaller(e.getCaller());
        vo.setModule(e.getModule());
        vo.setAction(e.getAction());
        vo.setActionName(e.getActionName());
        vo.setTargetType(e.getTargetType());
        vo.setTargetId(e.getTargetId());
        vo.setTargetName(e.getTargetName());
        vo.setContent(e.getContent());
        vo.setResult(e.getResult());
        vo.setErrorCode(e.getErrorCode());
        vo.setErrorMsg(e.getErrorMsg());
        vo.setCostMs(e.getCostMs());
        vo.setTraceId(e.getTraceId());
        return vo;
    }

    private static AuditChangeResp toResp(SysAuditLogChangeEntity e) {
        AuditChangeResp vo = new AuditChangeResp();
        vo.setFieldName(e.getFieldName());
        vo.setFieldLabel(e.getFieldLabel());
        vo.setOldValue(e.getOldValue());
        vo.setNewValue(e.getNewValue());
        vo.setOldDisplay(e.getOldDisplay());
        vo.setNewDisplay(e.getNewDisplay());
        vo.setChangedType(e.getChangedType());
        return vo;
    }
}
