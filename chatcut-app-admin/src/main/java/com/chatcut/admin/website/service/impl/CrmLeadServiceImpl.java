package com.chatcut.admin.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.website.enums.LeadStatusEnum;
import com.chatcut.admin.website.enums.LeadValidityEnum;
import com.chatcut.admin.website.model.request.CrmLeadListReq;
import com.chatcut.admin.website.model.request.CrmLeadNoteCreateReq;
import com.chatcut.admin.website.model.request.CrmLeadUpdateReq;
import com.chatcut.admin.website.model.response.CrmLeadDetailResp;
import com.chatcut.admin.website.model.response.CrmLeadNoteResp;
import com.chatcut.admin.website.model.response.CrmLeadResp;
import com.chatcut.admin.website.model.response.CrmLeadSubmissionResp;
import com.chatcut.admin.website.repository.entity.CrmLeadEntity;
import com.chatcut.admin.website.repository.entity.CrmLeadNoteEntity;
import com.chatcut.admin.website.repository.entity.CrmLeadSubmissionEntity;
import com.chatcut.admin.website.repository.mapper.CrmLeadMapper;
import com.chatcut.admin.website.repository.mapper.CrmLeadNoteMapper;
import com.chatcut.admin.website.repository.mapper.CrmLeadSubmissionMapper;
import com.chatcut.admin.website.service.CrmLeadService;
import com.chatcut.admin.website.support.WebsiteUtils;
import com.chatcut.admin.website.support.WebChangeLogRecorder;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 官网线索服务实现。状态机校验终态不可重开；联系方式明文返回、明文导出。
 */
@RequiredArgsConstructor
@Service
public class CrmLeadServiceImpl implements CrmLeadService {

    /** 导出上限保护：超过说明筛选范围过大，同步导出不再合适。 */
    private static final long EXPORT_LIMIT = 5000;

    private final CrmLeadMapper crmLeadMapper;
    private final CrmLeadSubmissionMapper crmLeadSubmissionMapper;
    private final CrmLeadNoteMapper crmLeadNoteMapper;
    private final WebChangeLogRecorder changeLogRecorder;

    @Override
    public TableDataInfo<CrmLeadResp> pageCrmLeads(CrmLeadListReq request, PageQuery pageQuery) {
        Page<CrmLeadResp> page = crmLeadMapper.selectVoPage(pageQuery.build(),
            buildQueryWrapper(request).orderByDesc(CrmLeadEntity::getCreatedAt).orderByDesc(CrmLeadEntity::getId));
        return TableDataInfo.build(page);
    }

    @Override
    public CrmLeadDetailResp getCrmLeadInfo(Long id) {
        CrmLeadEntity crmLead = requireLead(id);
        CrmLeadDetailResp resp = new CrmLeadDetailResp();
        resp.setLead(MapstructUtils.convert(crmLead, CrmLeadResp.class));
        resp.setSubmissions(crmLeadSubmissionMapper.selectVoList(new LambdaQueryWrapper<CrmLeadSubmissionEntity>()
            .eq(CrmLeadSubmissionEntity::getLeadId, id)
            .orderByDesc(CrmLeadSubmissionEntity::getSubmittedAt)));
        List<CrmLeadNoteResp> notes = crmLeadNoteMapper.selectVoList(new LambdaQueryWrapper<CrmLeadNoteEntity>()
            .eq(CrmLeadNoteEntity::getLeadId, id)
            .orderByAsc(CrmLeadNoteEntity::getCreatedAt));
        resp.setNotes(notes);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCrmLead(Long id, CrmLeadUpdateReq request) {
        CrmLeadEntity crmLead = requireLead(id);

        LeadStatusEnum from = LeadStatusEnum.of(crmLead.getStatus());
        String statusAfter = crmLead.getStatus();
        if (StringUtils.isNotBlank(request.getStatus())) {
            LeadStatusEnum to = LeadStatusEnum.of(request.getStatus());
            LeadStatusEnum.checkTransition(from, to);
            statusAfter = to.getCode();
        }
        if (StringUtils.isNotBlank(request.getValidity())) {
            LeadValidityEnum.of(request.getValidity());
        }

        Map<String, String[]> changes = new LinkedHashMap<>();
        putIfChanged(changes, "status", crmLead.getStatus(), statusAfter);
        putIfChanged(changes, "validity", crmLead.getValidity(), request.getValidity());
        Long ownerAdminId = WebsiteUtils.parseIdNullable(request.getOwnerAdminId());
        putIfChanged(changes, "owner_admin_id", Objects.toString(crmLead.getOwnerAdminId(), null),
            Objects.toString(ownerAdminId, null));
        putIfChanged(changes, "next_follow_at", Objects.toString(crmLead.getNextFollowAt(), null),
            Objects.toString(request.getNextFollowAt(), null));
        putIfChanged(changes, "result", crmLead.getResult(), request.getResult());

        if (StringUtils.isNotBlank(request.getStatus())) {
            crmLead.setStatus(statusAfter);
        }
        if (StringUtils.isNotBlank(request.getValidity())) {
            crmLead.setValidity(request.getValidity());
        }
        if (ownerAdminId != null) {
            crmLead.setOwnerAdminId(ownerAdminId);
        }
        if (request.getNextFollowAt() != null) {
            crmLead.setNextFollowAt(request.getNextFollowAt());
        }
        if (request.getResult() != null) {
            crmLead.setResult(request.getResult());
        }
        int rows = crmLeadMapper.updateById(crmLead);
        if (rows == 0) {
            throw new ServiceException("线索已被他人更新（版本冲突），请刷新后重试");
        }
        changeLogRecorder.record("LEAD", id, changes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCrmLeadNote(Long id, CrmLeadNoteCreateReq request) {
        CrmLeadEntity crmLead = requireLead(id);
        CrmLeadNoteEntity crmLeadNote = new CrmLeadNoteEntity();
        crmLeadNote.setLeadId(id);
        crmLeadNote.setAuthorAdminId(LoginHelper.getUserId());
        crmLeadNote.setNote(request.getNote());
        crmLeadNote.setCreatedAt(LocalDateTime.now());
        crmLeadNoteMapper.insert(crmLeadNote);
    }

    @Override
    public void exportCrmLeads(CrmLeadListReq request, HttpServletResponse response) throws Exception {
        long total = crmLeadMapper.selectCount(buildQueryWrapper(request));
        if (total > EXPORT_LIMIT) {
            throw new ServiceException("导出范围过大（" + total + " 条），请缩小筛选条件");
        }
        List<CrmLeadEntity> leads = crmLeadMapper.selectList(buildQueryWrapper(request)
            .orderByDesc(CrmLeadEntity::getCreatedAt));

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"线索编号", "公司名称", "联系人", "联系方式", "联系方式类型", "咨询意向",
            "来源", "状态", "有效性", "负责人ID", "下次跟进时间", "处理结果", "创建时间"});
        for (CrmLeadEntity crmLead : leads) {
            rows.add(new String[]{
                crmLead.getLeadNo(),
                crmLead.getCompanyName(),
                crmLead.getContactName(),
                crmLead.getContact(),
                crmLead.getContactType(),
                crmLead.getIntent(),
                crmLead.getSource(),
                crmLead.getStatus(),
                crmLead.getValidity(),
                Objects.toString(crmLead.getOwnerAdminId(), ""),
                Objects.toString(crmLead.getNextFollowAt(), ""),
                crmLead.getResult(),
                Objects.toString(crmLead.getCreatedAt(), "")
            });
        }
        WebsiteUtils.writeCsv(response, "crm_leads", rows);
    }

    // ------------------------------------------------------------------ private

    private LambdaQueryWrapper<CrmLeadEntity> buildQueryWrapper(CrmLeadListReq request) {
        Long ownerAdminId = WebsiteUtils.parseIdNullable(request.getOwnerAdminId());
        LambdaQueryWrapper<CrmLeadEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(request.getStatus()), CrmLeadEntity::getStatus, request.getStatus())
            .eq(StringUtils.isNotBlank(request.getIntent()), CrmLeadEntity::getIntent, request.getIntent())
            .eq(StringUtils.isNotBlank(request.getValidity()), CrmLeadEntity::getValidity, request.getValidity())
            .eq(ownerAdminId != null, CrmLeadEntity::getOwnerAdminId, ownerAdminId);
        if (StringUtils.isNotBlank(request.getKeyword())) {
            String kw = request.getKeyword().trim();
            wrapper.and(w -> w.like(CrmLeadEntity::getLeadNo, kw)
                .or().like(CrmLeadEntity::getCompanyName, kw)
                .or().like(CrmLeadEntity::getContactName, kw)
                .or().like(CrmLeadEntity::getContact, kw));
        }
        return wrapper;
    }

    private CrmLeadEntity requireLead(Long id) {
        CrmLeadEntity crmLead = crmLeadMapper.selectById(id);
        if (crmLead == null) {
            throw new ServiceException("线索不存在");
        }
        return crmLead;
    }

    private void putIfChanged(Map<String, String[]> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(field, new String[]{oldValue, newValue});
        }
    }
}
