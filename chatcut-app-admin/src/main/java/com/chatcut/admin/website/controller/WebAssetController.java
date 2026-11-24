package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.website.model.request.WebAssetDeleteReq;
import com.chatcut.admin.website.model.request.WebAssetListReq;
import com.chatcut.admin.website.model.request.WebAssetSaveReq;
import com.chatcut.admin.website.model.response.WebAssetResp;
import com.chatcut.admin.website.service.WebAssetService;
import com.chatcut.admin.website.service.WebAssetService.WebAssetDetail;
import com.chatcut.admin.website.support.WebsiteUtils;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.idempotent.annotation.RepeatSubmit;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 官网展示素材管理。删除仅逻辑删除，不删除 OSS 原文件；被引用素材禁止删除。
 * 前端 ID 统一传 String，路径不携带参数。
 */
@Tag(name = "官网素材", description = "官网展示素材的登记、查询与删除")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website/assets")
public class WebAssetController extends BaseController {

    private final WebAssetService webAssetService;

    @Operation(summary = "素材筛选分页")
    @SaCheckPermission("website:asset:list")
    @GetMapping("/listWebAssets")
    public TableDataInfo<WebAssetResp> listWebAssets(WebAssetListReq request, PageQuery pageQuery) {
        return webAssetService.pageWebAssets(request, pageQuery);
    }

    @Operation(summary = "素材详情及引用数")
    @SaCheckPermission("website:asset:list")
    @GetMapping("/getWebAssetInfo")
    public R<WebAssetDetail> getWebAssetInfo(@NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(webAssetService.getWebAssetInfo(WebsiteUtils.parseId(id)));
    }

    @Operation(summary = "登记素材")
    @SaCheckPermission("website:asset:manage")
    @Log(title = "官网素材", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/createWebAsset")
    public R<String> createWebAsset(@Validated @RequestBody WebAssetSaveReq request) {
        return R.ok("操作成功", String.valueOf(webAssetService.createWebAsset(request)));
    }

    @Operation(summary = "删除未引用素材（逻辑删除，保留 OSS 原文件）")
    @SaCheckPermission("website:asset:manage")
    @Log(title = "官网素材", businessType = BusinessType.DELETE)
    @RepeatSubmit
    @PostMapping("/deleteWebAsset")
    public R<Void> deleteWebAsset(@Validated @RequestBody WebAssetDeleteReq request) {
        webAssetService.deleteWebAsset(WebsiteUtils.parseId(request.getId()));
        return R.ok();
    }
}
