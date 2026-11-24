package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.website.model.request.WebSiteSettingSaveReq;
import com.chatcut.admin.website.model.response.WebSiteSettingResp;
import com.chatcut.admin.website.service.WebSiteSettingService;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.idempotent.annotation.RepeatSubmit;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 官网站点设置。保存后立即生效，官网实时查库，无预览或发布步骤。
 */
@Tag(name = "官网站点设置", description = "站点名称、联系与响应说明的读取与更新")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website/settings")
public class WebSiteSettingController extends BaseController {

    private final WebSiteSettingService webSiteSettingService;

    @Operation(summary = "读取站点配置")
    @SaCheckPermission("website:settings:read")
    @GetMapping("/getWebSiteSetting")
    public R<WebSiteSettingResp> getWebSiteSetting() {
        return R.ok(webSiteSettingService.getWebSiteSetting());
    }

    @Operation(summary = "更新站点配置（带乐观锁，保存立即生效）")
    @SaCheckPermission("website:settings:update")
    @Log(title = "官网站点设置", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/saveWebSiteSetting")
    public R<Void> saveWebSiteSetting(@Validated @RequestBody WebSiteSettingSaveReq request) {
        webSiteSettingService.saveWebSiteSetting(request);
        return R.ok();
    }
}
