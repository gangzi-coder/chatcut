package com.chatcut.admin.website.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.admin.website.model.request.WebSiteSettingSaveReq;
import com.chatcut.admin.website.model.response.WebSiteSettingResp;
import com.chatcut.admin.website.repository.entity.WebSiteSettingEntity;
import com.chatcut.admin.website.repository.mapper.WebSiteSettingMapper;
import com.chatcut.admin.website.service.WebSiteSettingService;
import com.chatcut.admin.website.support.WebChangeLogRecorder;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 官网站点设置服务实现。一期固定一行 setting_key = site_profile。
 * 更新走乐观锁（webContentVersion），更新 0 行视为版本冲突；无配置行时首次保存直接创建。
 */
@RequiredArgsConstructor
@Service
public class WebSiteSettingServiceImpl implements WebSiteSettingService {

    /** 一期固定的设置分组键。 */
    public static final String SETTING_KEY = "site_profile";

    private final WebSiteSettingMapper webSiteSettingMapper;
    private final WebChangeLogRecorder changeLogRecorder;

    @Override
    public WebSiteSettingResp getWebSiteSetting() {
        WebSiteSettingEntity webSiteSetting = webSiteSettingMapper.selectOne(new LambdaQueryWrapper<WebSiteSettingEntity>()
            .eq(WebSiteSettingEntity::getSettingKey, SETTING_KEY));
        return webSiteSetting == null ? null : MapstructUtils.convert(webSiteSetting, WebSiteSettingResp.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWebSiteSetting(WebSiteSettingSaveReq request) {
        Long adminId = LoginHelper.getUserId();
        LocalDateTime now = LocalDateTime.now();
        WebSiteSettingEntity existing = webSiteSettingMapper.selectOne(new LambdaQueryWrapper<WebSiteSettingEntity>()
            .eq(WebSiteSettingEntity::getSettingKey, SETTING_KEY));

        if (existing == null) {
            WebSiteSettingEntity webSiteSetting = new WebSiteSettingEntity();
            webSiteSetting.setSettingKey(SETTING_KEY);
            webSiteSetting.setBrand(request.getBrand());
            webSiteSetting.setContactEmail(request.getContactEmail());
            webSiteSetting.setContactPhone(request.getContactPhone());
            webSiteSetting.setResponse(request.getResponse());
            webSiteSetting.setVersion(0);
            webSiteSetting.setUpdatedBy(adminId);
            webSiteSetting.setUpdatedAt(now);
            webSiteSettingMapper.insert(webSiteSetting);
            return;
        }

        // 前端传入 webContentVersion 必须与当前一致，防止基于旧页面的修改静默覆盖他人变更
        if (request.getVersion() == null || !Objects.equals(request.getVersion(), existing.getVersion())) {
            throw new ServiceException("站点设置已被他人修改（版本冲突），请刷新后重试");
        }

        Map<String, String[]> changes = new LinkedHashMap<>();
        putIfChanged(changes, "brand", existing.getBrand(), request.getBrand());
        putIfChanged(changes, "contact_email", existing.getContactEmail(), request.getContactEmail());
        putIfChanged(changes, "contact_phone", existing.getContactPhone(), request.getContactPhone());
        putIfChanged(changes, "response", existing.getResponse(), request.getResponse());

        existing.setBrand(request.getBrand());
        existing.setContactEmail(request.getContactEmail());
        existing.setContactPhone(request.getContactPhone());
        existing.setResponse(request.getResponse());
        existing.setUpdatedBy(adminId);
        existing.setUpdatedAt(now);
        int rows = webSiteSettingMapper.updateById(existing);
        if (rows == 0) {
            throw new ServiceException("站点设置已被他人修改（版本冲突），请刷新后重试");
        }
        changeLogRecorder.record("SETTING", existing.getId(), changes);
    }

    private void putIfChanged(Map<String, String[]> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(field, new String[]{oldValue, newValue});
        }
    }
}
