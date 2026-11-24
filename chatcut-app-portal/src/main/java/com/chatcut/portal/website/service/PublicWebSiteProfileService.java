package com.chatcut.portal.website.service;

import com.chatcut.portal.website.model.response.PublicWebSiteProfileResp;

/**
 * 官网公开站点配置服务。
 */
public interface PublicWebSiteProfileService {

    /**
     * 公开站点配置：只返回品牌名、联系邮箱、联系电话与响应时效说明等公开列。
     */
    PublicWebSiteProfileResp getPublicWebSiteProfile();
}
