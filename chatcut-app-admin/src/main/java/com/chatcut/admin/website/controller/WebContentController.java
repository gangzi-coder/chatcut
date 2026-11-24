package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.website.model.request.WebContentActionReq;
import com.chatcut.admin.website.model.request.WebContentCreateReq;
import com.chatcut.admin.website.model.request.WebContentDraftReq;
import com.chatcut.admin.website.model.request.WebContentListReq;
import com.chatcut.admin.website.model.request.WebContentVersionRestoreReq;
import com.chatcut.admin.website.model.response.WebContentDetailResp;
import com.chatcut.admin.website.model.response.WebContentResp;
import com.chatcut.admin.website.model.response.WebContentVersionResp;
import com.chatcut.admin.website.service.WebContentService;
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

import java.util.List;

/**
 * 官网内容管理。网关 /admin/website/** StripPrefix 后落到本控制器。
 *
 * <p>草稿与正式发布版本分离：保存不影响官网，发布切版本，下线保护站内引用，
 * 恢复历史只写回草稿、仍需再次发布。前端 ID 统一传 String，路径不携带参数。</p>
 */
@Tag(name = "官网内容", description = "官网内容的编辑、发布、下线与版本管理")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website/contents")
public class WebContentController extends BaseController {

    private final WebContentService webContentService;

    @Operation(summary = "内容筛选分页")
    @SaCheckPermission("website:content:list")
    @GetMapping("/listWebContents")
    public TableDataInfo<WebContentResp> listWebContents(WebContentListReq request, PageQuery pageQuery) {
        return webContentService.pageWebContents(request, pageQuery);
    }

    @Operation(summary = "内容详情（含草稿与当前发布版本）")
    @SaCheckPermission("website:content:detail")
    @GetMapping("/getWebContentInfo")
    public R<WebContentDetailResp> getWebContentInfo(@NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(webContentService.getWebContentInfo(WebsiteUtils.parseId(id)));
    }

    @Operation(summary = "新建内容并写入初始草稿")
    @SaCheckPermission("website:content:save")
    @Log(title = "官网内容", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/createWebContent")
    public R<String> createWebContent(@Validated @RequestBody WebContentCreateReq request) {
        // 注意：R.ok(String) 会命中 msg 重载，必须用 ok(msg, data) 两参形式放 data
        return R.ok("操作成功", String.valueOf(webContentService.createWebContent(request)));
    }

    @Operation(summary = "更新草稿（保存不影响官网）")
    @SaCheckPermission("website:content:save")
    @Log(title = "官网内容", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/saveWebContentDraft")
    public R<Void> saveWebContentDraft(@Validated @RequestBody WebContentDraftReq request) {
        webContentService.saveWebContentDraft(WebsiteUtils.parseId(request.getId()), request);
        return R.ok();
    }

    @Operation(summary = "校验并发布（事务内新增版本并切换当前发布版本）")
    @SaCheckPermission("website:content:publish")
    @Log(title = "官网内容", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/publishWebContent")
    public R<Integer> publishWebContent(@Validated @RequestBody WebContentActionReq request) {
        return R.ok(webContentService.publishWebContent(WebsiteUtils.parseId(request.getId())));
    }

    @Operation(summary = "下线内容（校验其他已发布内容的站内引用）")
    @SaCheckPermission("website:content:offline")
    @Log(title = "官网内容", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/offlineWebContent")
    public R<Void> offlineWebContent(@Validated @RequestBody WebContentActionReq request) {
        webContentService.offlineWebContent(WebsiteUtils.parseId(request.getId()));
        return R.ok();
    }

    @Operation(summary = "版本列表（按版本号倒序）")
    @SaCheckPermission("website:content:versions")
    @GetMapping("/listWebContentVersions")
    public R<List<WebContentVersionResp>> listWebContentVersions(
        @NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(webContentService.listWebContentVersions(WebsiteUtils.parseId(id)));
    }

    @Operation(summary = "恢复历史版本至草稿（仍需再次发布）")
    @SaCheckPermission("website:content:restore")
    @Log(title = "官网内容", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/restoreWebContentVersion")
    public R<Void> restoreWebContentVersion(@Validated @RequestBody WebContentVersionRestoreReq request) {
        webContentService.restoreWebContentVersion(WebsiteUtils.parseId(request.getId()), request.getVersionNo());
        return R.ok();
    }
}
