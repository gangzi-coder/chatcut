package com.chatcut.admin.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.admin.website.enums.ContentStatusEnum;
import com.chatcut.admin.website.enums.FeedbackStatusEnum;
import com.chatcut.admin.website.enums.LeadStatusEnum;
import com.chatcut.admin.website.enums.LeadValidityEnum;
import com.chatcut.admin.website.model.response.WebOverviewResp;
import com.chatcut.admin.website.repository.entity.CrmFeedbackEntity;
import com.chatcut.admin.website.repository.entity.CrmLeadEntity;
import com.chatcut.admin.website.repository.entity.WebContentEntity;
import com.chatcut.admin.website.repository.mapper.CrmFeedbackMapper;
import com.chatcut.admin.website.repository.mapper.CrmLeadMapper;
import com.chatcut.admin.website.repository.mapper.WebContentMapper;
import com.chatcut.admin.website.service.WebOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 工作概览服务实现。访问量小，全部实时 count，不引入缓存。
 */
@RequiredArgsConstructor
@Service
public class WebOverviewServiceImpl implements WebOverviewService {

    private final CrmLeadMapper crmLeadMapper;
    private final CrmFeedbackMapper crmFeedbackMapper;
    private final WebContentMapper webContentMapper;

    @Override
    public WebOverviewResp getWebOverview() {
        WebOverviewResp resp = new WebOverviewResp();
        resp.setPendingLeads(Math.toIntExact(crmLeadMapper.selectCount(new LambdaQueryWrapper<CrmLeadEntity>()
            .eq(CrmLeadEntity::getStatus, LeadStatusEnum.PENDING.getCode()))));
        resp.setValidLeads(Math.toIntExact(crmLeadMapper.selectCount(new LambdaQueryWrapper<CrmLeadEntity>()
            .eq(CrmLeadEntity::getValidity, LeadValidityEnum.VALID.getCode()))));
        resp.setPendingFeedbacks(Math.toIntExact(crmFeedbackMapper.selectCount(new LambdaQueryWrapper<CrmFeedbackEntity>()
            .eq(CrmFeedbackEntity::getStatus, FeedbackStatusEnum.PENDING.getCode()))));
        resp.setPublishedContents(Math.toIntExact(webContentMapper.selectCount(new LambdaQueryWrapper<WebContentEntity>()
            .eq(WebContentEntity::getStatus, ContentStatusEnum.PUBLISHED.getCode()))));
        resp.setDraftContents(Math.toIntExact(webContentMapper.selectCount(new LambdaQueryWrapper<WebContentEntity>()
            .eq(WebContentEntity::getStatus, ContentStatusEnum.DRAFT.getCode()))));
        return resp;
    }
}
