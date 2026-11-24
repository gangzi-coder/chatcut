package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.request.CrmLeadListReq;
import com.chatcut.admin.website.model.request.CrmLeadNoteCreateReq;
import com.chatcut.admin.website.model.request.CrmLeadUpdateReq;
import com.chatcut.admin.website.model.response.CrmLeadDetailResp;
import com.chatcut.admin.website.model.response.CrmLeadResp;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 官网线索服务。不做合并，每次留资独立成线索；终态不可重开。
 */
public interface CrmLeadService {

    /** 线索筛选分页（状态/意向/有效性/负责人/关键词）。 */
    TableDataInfo<CrmLeadResp> pageCrmLeads(CrmLeadListReq request, PageQuery pageQuery);

    /** 线索详情：线索 + 提交历史 + 跟进记录。 */
    CrmLeadDetailResp getCrmLeadInfo(Long id);

    /** 更新线索：分配、状态机、有效性、下次跟进、处理结果（乐观锁）。 */
    void updateCrmLead(Long id, CrmLeadUpdateReq request);

    /** 添加内部跟进记录（追加不可编辑/删除）。 */
    void addCrmLeadNote(Long id, CrmLeadNoteCreateReq request);

    /** 按当前筛选同步导出 CSV（明文，预计不超过 1000 条）。 */
    void exportCrmLeads(CrmLeadListReq request, HttpServletResponse response) throws Exception;
}
