package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.request.CrmFeedbackConvertReq;
import com.chatcut.admin.website.model.request.CrmFeedbackListReq;
import com.chatcut.admin.website.model.request.CrmFeedbackUpdateReq;
import com.chatcut.admin.website.model.response.CrmFeedbackResp;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;

/**
 * 官网访客反馈服务。转线索不改变反馈状态，只建立关联；终态不可重开。
 */
public interface CrmFeedbackService {

    /** 反馈筛选分页。 */
    TableDataInfo<CrmFeedbackResp> pageCrmFeedbacks(CrmFeedbackListReq request, PageQuery pageQuery);

    /** 反馈详情。 */
    CrmFeedbackResp getCrmFeedbackInfo(Long id);

    /** 更新反馈：分配、状态机、处理结果（乐观锁）。 */
    void updateCrmFeedback(Long id, CrmFeedbackUpdateReq request);

    /** 反馈转线索：同一事务内创建线索、写提交记录、建立关联；反馈状态保持原状态。 */
    Long convertCrmFeedbackToLead(Long id, CrmFeedbackConvertReq request);
}
