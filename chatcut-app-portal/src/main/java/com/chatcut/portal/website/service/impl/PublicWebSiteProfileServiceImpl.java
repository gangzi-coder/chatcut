package com.chatcut.portal.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.portal.website.model.response.PublicWebSiteProfileResp;
import com.chatcut.portal.website.repository.entity.WebSiteSettingEntity;
import com.chatcut.portal.website.repository.mapper.WebSiteSettingMapper;
import com.chatcut.portal.website.service.PublicWebSiteProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 官网公开站点配置服务实现。实时查库，只映射公开列。
 */
@RequiredArgsConstructor
@Service
public class PublicWebSiteProfileServiceImpl implements PublicWebSiteProfileService {

    private final WebSiteSettingMapper webSiteSettingMapper;

    @Override
    public PublicWebSiteProfileResp getPublicWebSiteProfile() {
        WebSiteSettingEntity webSiteSetting = webSiteSettingMapper.selectOne(
            new LambdaQueryWrapper<WebSiteSettingEntity>()
                .eq(WebSiteSettingEntity::getSettingKey, "site_profile"));
        if (webSiteSetting == null) {
            throw new ServiceException("站点配置未初始化");
        }
        PublicWebSiteProfileResp resp = new PublicWebSiteProfileResp();
        resp.setBrand(webSiteSetting.getBrand());
        resp.setContactEmail(webSiteSetting.getContactEmail());
        resp.setContactPhone(webSiteSetting.getContactPhone());
        resp.setResponse(webSiteSetting.getResponse());
        return resp;
    }
}
