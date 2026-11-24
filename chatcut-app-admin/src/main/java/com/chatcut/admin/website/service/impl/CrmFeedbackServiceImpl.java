package com.chatcut.admin.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.website.enums.ContactTypeEnum;
import com.chatcut.admin.website.enums.FeedbackStatusEnum;
import com.chatcut.admin.website.enums.LeadIntentEnum;
import com.chatcut.admin.website.enums.LeadStatusEnum;
import com.chatcut.admin.website.enums.LeadValidityEnum;
import com.chatcut.admin.website.model.request.CrmFeedbackConvertReq;
import com.chatcut.admin.website.model.request.CrmFeedbackListReq;
import com.chatcut.admin.website.model.request.CrmFeedbackUpdateReq;
import com.chatcut.admin.website.model.response.CrmFeedbackResp;
import com.chatcut.admin.website.repository.entity.CrmFeedbackEntity;
import com.chatcut.admin.website.repository.entity.CrmLeadEntity;
import com.chatcut.admin.website.repository.entity.CrmLeadSubmissionEntity;
import com.chatcut.admin.website.repository.mapper.CrmFeedbackMapper;
import com.chatcut.admin.website.repository.mapper.CrmLeadMapper;
import com.chatcut.admin.website.repository.mapper.CrmLeadSubmissionMapper;
import com.chatcut.admin.website.service.CrmFeedbackService;
import com.chatcut.admin.website.support.WebChangeLogRecorder;
import com.chatcut.admin.website.support.WebsiteUtils;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 官网访客反馈服务实现。转化与关联在同一事务完成，保留反馈原文，状态不变。
 */
@RequiredArgsConstructor
@Service
public class CrmFeedbackServiceImpl implements CrmFeedbackService {

    private final CrmFeedbackMapper crmFeedbackMapper;
    private final CrmLeadMapper crmLeadMapper;
    private final CrmLeadSubmissionMapper crmLeadSubmissionMapper;
    private final WebChangeLogRecorder changeLogRecorder;

    @Override
    public TableDataInfo<CrmFeedbackResp> pageCrmFeedbacks(CrmFeedbackListReq request, PageQuery pageQuery) {
        LambdaQueryWrapper<CrmFeedbackEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(request.getStatus()), CrmFeedbackEntity::getStatus, request.getStatus())
            .eq(StringUtils.isNotBlank(request.getCategory()), CrmFeedbackEntity::getCategory, request.getCategory());
        if (StringUtils.isNotBlank(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            wrapper.and(w -> w.like(CrmFeedbackEntity::getBody, kw)
                .or().like(CrmFeedbackEntity::getContact, kw));
        }
        wrapper.orderByDesc(CrmFeedbackEntity::getCreatedAt);
        Page<CrmFeedbackResp> page = crmFeedbackMapper.selectVoPage(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public CrmFeedbackResp getCrmFeedbackInfo(Long id) {
        return MapstructUtils.convert(requireFeedback(id), CrmFeedbackResp.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCrmFeedback(Long id, CrmFeedbackUpdateReq request) {
        CrmFeedbackEntity crmFeedback = requireFeedback(id);

        FeedbackStatusEnum from = FeedbackStatusEnum.of(crmFeedback.getStatus());
        String statusAfter = crmFeedback.getStatus();
        if (StringUtils.isNotBlank(request.getStatus())) {
            FeedbackStatusEnum to = FeedbackStatusEnum.of(request.getStatus());
            FeedbackStatusEnum.checkTransition(from, to);
            statusAfter = to.getCode();
        }

        Long ownerAdminId = WebsiteUtils.parseIdNullable(request.getOwnerAdminId());
        Map<String, String[]> changes = new LinkedHashMap<>();
        putIfChanged(changes, "status", crmFeedback.getStatus(), statusAfter);
        putIfChanged(changes, "owner_admin_id", Objects.toString(crmFeedback.getOwnerAdminId(), null),
            Objects.toString(ownerAdminId, null));
        putIfChanged(changes, "result", crmFeedback.getResult(), request.getResult());

        if (StringUtils.isNotBlank(request.getStatus())) {
            crmFeedback.setStatus(statusAfter);
        }
        if (ownerAdminId != null) {
            crmFeedback.setOwnerAdminId(ownerAdminId);
        }
        if (request.getResult() != null) {
            crmFeedback.setResult(request.getResult());
        }
        crmFeedback.setUpdatedAt(LocalDateTime.now());
        int rows = crmFeedbackMapper.updateById(crmFeedback);
        if (rows == 0) {
            throw new ServiceException("反馈已被他人更新（版本冲突），请刷新后重试");
        }
        changeLogRecorder.record("FEEDBACK", id, changes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertCrmFeedbackToLead(Long id, CrmFeedbackConvertReq request) {
        CrmFeedbackEntity crmFeedback = requireFeedback(id);
        if (crmFeedback.getLeadId() != null) {
            throw new ServiceException("该反馈已关联线索，不能重复转化");
        }
        LeadIntentEnum.of(request.getIntent());
        ContactTypeEnum.of(request.getContactType());

        LocalDateTime now = LocalDateTime.now();
        Long adminId = LoginHelper.getUserId();

        // 创建线索：每次转化独立成线索，不做合并
        CrmLeadEntity crmLead = new CrmLeadEntity();
        crmLead.setLeadNo(WebsiteUtils.nextLeadNo());
        crmLead.setCompanyName(request.getCompanyName());
        crmLead.setContactName(request.getContactName());
        crmLead.setContact(request.getContact());
        crmLead.setContactType(request.getContactType());
        crmLead.setIntent(request.getIntent());
        crmLead.setSource(StringUtils.isNotBlank(request.getSource()) ? request.getSource() : crmFeedback.getSource());
        crmLead.setStatus(LeadStatusEnum.PENDING.getCode());
        crmLead.setValidity(LeadValidityEnum.UNCONFIRMED.getCode());
        crmLead.setCreatedAt(now);
        crmLead.setUpdatedAt(now);
        crmLeadMapper.insert(crmLead);

        // 写提交记录（保留反馈原文；request_id 加前缀防与访客直接提交撞唯一键）
        CrmLeadSubmissionEntity crmLeadSubmission = new CrmLeadSubmissionEntity();
        crmLeadSubmission.setRequestId(truncate("FB-" + crmFeedback.getRequestId(), 64));
        crmLeadSubmission.setCaptchaToken(null);
        crmLeadSubmission.setLeadId(crmLead.getId());
        crmLeadSubmission.setSource(crmLead.getSource());
        crmLeadSubmission.setContent(crmFeedback.getBody());
        crmLeadSubmission.setSubmittedAt(now);
        crmLeadSubmissionMapper.insert(crmLeadSubmission);

        // 建立关联；反馈状态保持原状态，不强制置为完成
        crmFeedback.setLeadId(crmLead.getId());
        crmFeedback.setUpdatedAt(now);
        int rows = crmFeedbackMapper.updateById(crmFeedback);
        if (rows == 0) {
            throw new ServiceException("反馈已被他人更新（版本冲突），请刷新后重试");
        }

        return crmLead.getId();
    }

    // ------------------------------------------------------------------ private

    private CrmFeedbackEntity requireFeedback(Long id) {
        CrmFeedbackEntity crmFeedback = crmFeedbackMapper.selectById(id);
        if (crmFeedback == null) {
            throw new ServiceException("反馈不存在");
        }
        return crmFeedback;
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private void putIfChanged(Map<String, String[]> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(field, new String[]{oldValue, newValue});
        }
    }
}
