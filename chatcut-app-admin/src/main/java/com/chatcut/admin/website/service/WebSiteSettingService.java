package com.chatcut.admin.website.service;

import com.chatcut.admin.website.model.request.WebSiteSettingSaveReq;
import com.chatcut.admin.website.model.response.WebSiteSettingResp;

/**
 * 官网站点设置服务。配置摊平为列，保存即生效，官网实时查库。
 */
public interface WebSiteSettingService {

    /** 读取站点配置（setting_key = site_profile），无配置返回 null。 */
    WebSiteSettingResp getWebSiteSetting();

    /** 更新站点配置：乐观锁校验，字段级变更写 web_change_log，保存后立即生效。 */
    void saveWebSiteSetting(WebSiteSettingSaveReq request);
}
