package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.response.WebOverviewResp;

/**
 * 工作概览服务。实时聚合，不做缓存。
 */
public interface WebOverviewService {

    /** 概览计数：待处理线索、有效线索、待处理反馈、已发布内容、待发布草稿。 */
    WebOverviewResp getWebOverview();
}
