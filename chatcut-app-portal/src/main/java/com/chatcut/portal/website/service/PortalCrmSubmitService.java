package com.chatcut.portal.website.service;

import com.chatcut.portal.website.model.request.PortalCrmFeedbackSubmitReq;
import com.chatcut.portal.website.model.request.PortalCrmLeadSubmitReq;
import com.chatcut.portal.website.model.response.PortalCrmFeedbackSubmitResp;
import com.chatcut.portal.website.model.response.PortalCrmLeadSubmitResp;

/**
 * portal 访客提交服务：留资与反馈，免登录，含限流/令牌校验/防重复提交。
 */
public interface PortalCrmSubmitService {

    /**
     * 访客留资：写 crm_lead + crm_lead_submission（同事务）。
     * 企业类意向强制联系人+公司；requestId/captchaToken 唯一键兜底防重复。
     *
     * @param request 留资请求
     */
    PortalCrmLeadSubmitResp submitCrmLead(PortalCrmLeadSubmitReq request);

    /**
     * 访客反馈提交：写 crm_feedback，初始状态 PENDING。
     *
     * @param request 反馈请求
     */
    PortalCrmFeedbackSubmitResp submitCrmFeedback(PortalCrmFeedbackSubmitReq request);
}
