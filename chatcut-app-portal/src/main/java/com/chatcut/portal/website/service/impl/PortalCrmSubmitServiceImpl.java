package com.chatcut.portal.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.ServletUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.portal.website.enums.ContactTypeEnum;
import com.chatcut.portal.website.enums.FeedbackCategoryEnum;
import com.chatcut.portal.website.enums.LeadIntentEnum;
import com.chatcut.portal.website.repository.entity.CrmFeedbackEntity;
import com.chatcut.portal.website.repository.entity.CrmLeadEntity;
import com.chatcut.portal.website.repository.entity.CrmLeadSubmissionEntity;
import com.chatcut.portal.website.repository.mapper.CrmFeedbackMapper;
import com.chatcut.portal.website.repository.mapper.CrmLeadMapper;
import com.chatcut.portal.website.repository.mapper.CrmLeadSubmissionMapper;
import com.chatcut.portal.website.model.request.PortalCrmFeedbackSubmitReq;
import com.chatcut.portal.website.model.request.PortalCrmLeadSubmitReq;
import com.chatcut.portal.website.model.response.PortalCrmFeedbackSubmitResp;
import com.chatcut.portal.website.model.response.PortalCrmLeadSubmitResp;
import com.chatcut.portal.website.service.PortalCrmSubmitService;
import com.chatcut.portal.website.support.CaptchaTokenManager;
import com.chatcut.portal.website.support.PortalWebsiteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * portal 访客提交服务实现（留资 + 反馈）。
 *
 * <p>防护链路：每 IP Redis 限流（controller 层 @RateLimiter/Redisson）→ 验证码令牌签名/有效期校验
 * → 输入校验（枚举、企业类意向必填、联系方式格式）→ HTML 转义与转义后列宽校验 → 事务写库；
 * 重复提交由 requestId/captchaToken 唯一键兜底，冲突时返回幂等成功语义。</p>
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PortalCrmSubmitServiceImpl implements PortalCrmSubmitService {

    /** 列宽常量，与 sql/02_website_crm.sql 建表语句对齐；用于转义后长度校验。 */
    private static final int CRM_COMPANY_NAME_MAX = 120;

    private static final int CRM_CONTACT_NAME_MAX = 80;

    private static final int CRM_SOURCE_MAX = 200;

    private static final int SUBMISSION_CONTENT_MAX = 4000;

    private static final int FEEDBACK_BODY_MAX = 4000;

    private final CrmLeadMapper crmLeadMapper;
    private final CrmLeadSubmissionMapper crmLeadSubmissionMapper;
    private final CrmFeedbackMapper crmFeedbackMapper;
    private final CaptchaTokenManager captchaTokenManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalCrmLeadSubmitResp submitCrmLead(PortalCrmLeadSubmitReq request) {
        if (!captchaTokenManager.verifyPair(request.getCaptchaToken(), request.getRequestId())) {
            throw new ServiceException("验证码凭据无效或已过期，请重新获取");
        }

        LeadIntentEnum intent = LeadIntentEnum.of(request.getIntent());
        if (intent.isEnterpriseIntent()) {
            if (StringUtils.isBlank(request.getContactName()) || StringUtils.isBlank(request.getCompanyName())) {
                throw new ServiceException("该意向需填写联系人姓名与公司名称");
            }
        }
        ContactTypeEnum contactType = ContactTypeEnum.of(request.getContactType());
        PortalWebsiteUtils.validateContact(contactType, request.getContact());

        // 幂等预检：同 requestId 已提交过 → 直接返回原线索语义（唯一键仍兜底并发窗口）。
        // 必须先于凭据已核销检查：网络超时重试会原样带上已核销的凭据对，此时应幂等成功而非报错。
        PortalCrmLeadSubmitResp duplicated = resolveDuplicatedLead(request.getRequestId());
        if (duplicated != null) {
            return duplicated;
        }
        // 凭据一次性预检：token 或 requestId 任一已核销 → 拒绝
        // （crm_lead_submission 双唯一键 request_id/captcha_token 权威兜底并发窗口）
        if (captchaTokenManager.isConsumed(request.getCaptchaToken(), request.getRequestId())) {
            throw new ServiceException("验证码凭据已使用，请重新获取");
        }
        Long usedCount = crmLeadSubmissionMapper.selectCount(new LambdaQueryWrapper<CrmLeadSubmissionEntity>()
            .eq(CrmLeadSubmissionEntity::getCaptchaToken, request.getCaptchaToken()));
        if (usedCount != null && usedCount > 0) {
            throw new ServiceException("验证码凭据已使用，请重新获取");
        }

        LocalDateTime now = LocalDateTime.now();
        // 访客自由文本入库前统一 HTML 转义；联系方式被格式正则约束，无需转义
        CrmLeadEntity crmLead = new CrmLeadEntity();
        // 转义 + 转义后列宽校验（列宽与 sql/02_website_crm.sql 对齐，防止转义膨胀撑爆列宽裸 500）
        crmLead.setCompanyName(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getCompanyName(), CRM_COMPANY_NAME_MAX));
        crmLead.setContactName(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getContactName(), CRM_CONTACT_NAME_MAX));
        crmLead.setContact(request.getContact().trim());
        crmLead.setContactType(contactType.getCode());
        crmLead.setIntent(intent.getCode());
        crmLead.setSource(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getSource(), CRM_SOURCE_MAX));
        crmLead.setStatus("PENDING");
        crmLead.setValidity("UNCONFIRMED");
        crmLead.setCreatedAt(now);
        crmLead.setUpdatedAt(now);
        insertCrmLeadWithLeadNoRetry(crmLead);

        CrmLeadSubmissionEntity crmLeadSubmission = new CrmLeadSubmissionEntity();
        crmLeadSubmission.setRequestId(request.getRequestId().trim());
        crmLeadSubmission.setCaptchaToken(request.getCaptchaToken());
        crmLeadSubmission.setLeadId(crmLead.getId());
        crmLeadSubmission.setSource(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getSource(), CRM_SOURCE_MAX));
        crmLeadSubmission.setContent(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getContent(), SUBMISSION_CONTENT_MAX));
        crmLeadSubmission.setSubmittedAt(now);
        crmLeadSubmission.setIpHash(captchaTokenManager.ipHash(ServletUtils.getClientIP()));
        try {
            crmLeadSubmissionMapper.insert(crmLeadSubmission);
        } catch (DuplicateKeyException e) {
            // 并发窗口兜底：requestId 冲突 → 幂等成功；captchaToken 冲突 → 业务拒绝。
            // 极小概率：并发同 requestId 已额外落一条独立线索，一期按唯一键语义接受。
            PortalCrmLeadSubmitResp raced = resolveDuplicatedLead(request.getRequestId());
            if (raced != null) {
                log.warn("留资并发重复提交被唯一键拦截，requestId={}", request.getRequestId());
                return raced;
            }
            throw new ServiceException("验证码凭据已使用，请重新获取");
        }

        // 写库成功后核销凭据对（Redis 原子标记 token 与 requestId 两条，多实例共享；
        // 留资另由 DB 双唯一键权威兜底）
        captchaTokenManager.consume(request.getCaptchaToken(), request.getRequestId());

        PortalCrmLeadSubmitResp resp = new PortalCrmLeadSubmitResp();
        resp.setLeadNo(crmLead.getLeadNo());
        resp.setSubmittedAt(now);
        resp.setDuplicate(false);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PortalCrmFeedbackSubmitResp submitCrmFeedback(PortalCrmFeedbackSubmitReq request) {
        if (!captchaTokenManager.verifyPair(request.getCaptchaToken(), request.getRequestId())) {
            throw new ServiceException("验证码凭据无效或已过期，请重新获取");
        }

        FeedbackCategoryEnum.of(request.getCategory());
        // 联系方式可空；填写时类型必填且格式校验
        boolean contactFilled = StringUtils.isNotBlank(request.getContact());
        ContactTypeEnum contactType = null;
        if (contactFilled) {
            if (StringUtils.isBlank(request.getContactType())) {
                throw new ServiceException("填写联系方式时必须同时选择联系方式类型");
            }
            contactType = ContactTypeEnum.of(request.getContactType());
            PortalWebsiteUtils.validateContact(contactType, request.getContact());
        }

        // 幂等预检：crm_feedback.request_id 唯一键兜底（先于令牌核销检查，理由同留资）
        Long duplicatedCount = crmFeedbackMapper.selectCount(new LambdaQueryWrapper<CrmFeedbackEntity>()
            .eq(CrmFeedbackEntity::getRequestId, request.getRequestId().trim()));
        if (duplicatedCount != null && duplicatedCount > 0) {
            PortalCrmFeedbackSubmitResp resp = new PortalCrmFeedbackSubmitResp();
            resp.setDuplicate(true);
            return resp;
        }
        if (captchaTokenManager.isConsumed(request.getCaptchaToken(), request.getRequestId())) {
            throw new ServiceException("验证码凭据已使用，请重新获取");
        }

        // 反馈正文/来源入库前统一 HTML 转义 + 转义后列宽校验（列宽与 sql/02_website_crm.sql 对齐）
        CrmFeedbackEntity crmFeedback = new CrmFeedbackEntity();
        crmFeedback.setRequestId(request.getRequestId().trim());
        crmFeedback.setCategory(FeedbackCategoryEnum.of(request.getCategory()).getCode());
        crmFeedback.setBody(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getBody(), FEEDBACK_BODY_MAX));
        crmFeedback.setContact(contactFilled ? request.getContact().trim() : null);
        crmFeedback.setContactType(contactType == null ? null : contactType.getCode());
        crmFeedback.setSource(PortalWebsiteUtils.escapeTextWithColumnLimit(request.getSource(), CRM_SOURCE_MAX));
        crmFeedback.setStatus("PENDING");
        crmFeedback.setCreatedAt(LocalDateTime.now());
        crmFeedback.setUpdatedAt(LocalDateTime.now());
        try {
            crmFeedbackMapper.insert(crmFeedback);
        } catch (DuplicateKeyException e) {
            // 并发窗口兜底：requestId 冲突 → 幂等成功
            log.warn("反馈并发重复提交被唯一键拦截，requestId={}", request.getRequestId());
            PortalCrmFeedbackSubmitResp resp = new PortalCrmFeedbackSubmitResp();
            resp.setDuplicate(true);
            return resp;
        }

        // 写库成功后核销凭据对（Redis 原子标记 token 与 requestId 两条，多实例共享；
        // 重启不丢失，requestId 唯一键仍兜底同请求重放）
        captchaTokenManager.consume(request.getCaptchaToken(), request.getRequestId());

        PortalCrmFeedbackSubmitResp resp = new PortalCrmFeedbackSubmitResp();
        resp.setDuplicate(false);
        return resp;
    }

    // ------------------------------------------------------------------ private

    /**
     * 写入线索主表，lead_no 撞号（uk_crm_lead_no 唯一键冲突，多实例并发低概率事件）
     * 换号重试，最多 3 次；非撞号冲突原样抛出。
     */
    private void insertCrmLeadWithLeadNoRetry(CrmLeadEntity crmLead) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            crmLead.setLeadNo(PortalWebsiteUtils.nextLeadNo());
            try {
                crmLeadMapper.insert(crmLead);
                return;
            } catch (DuplicateKeyException e) {
                if (attempt == maxAttempts || !String.valueOf(e.getMessage()).contains("uk_crm_lead_no")) {
                    throw e;
                }
                log.warn("线索编号撞号（第 {} 次重试），oldLeadNo={}", attempt, crmLead.getLeadNo());
            }
        }
    }

    /**
     * 幂等返回原线索语义：requestId 已存在时反查提交记录与线索编号。
     * 拿不到原线索编号（理论不可达）时也按重复成功返回，避免给访客报错。
     */
    private PortalCrmLeadSubmitResp resolveDuplicatedLead(String requestId) {
        CrmLeadSubmissionEntity existed = crmLeadSubmissionMapper.selectOne(
            new LambdaQueryWrapper<CrmLeadSubmissionEntity>()
                .eq(CrmLeadSubmissionEntity::getRequestId, requestId.trim()));
        if (existed == null) {
            return null;
        }
        PortalCrmLeadSubmitResp resp = new PortalCrmLeadSubmitResp();
        CrmLeadEntity crmLead = crmLeadMapper.selectById(existed.getLeadId());
        if (crmLead != null) {
            resp.setLeadNo(crmLead.getLeadNo());
            resp.setSubmittedAt(existed.getSubmittedAt());
        }
        resp.setDuplicate(true);
        return resp;
    }
}
