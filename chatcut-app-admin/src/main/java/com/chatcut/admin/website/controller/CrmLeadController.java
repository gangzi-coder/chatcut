package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.website.model.request.CrmLeadListReq;
import com.chatcut.admin.website.model.request.CrmLeadNoteCreateReq;
import com.chatcut.admin.website.model.request.CrmLeadUpdateReq;
import com.chatcut.admin.website.model.response.CrmLeadDetailResp;
import com.chatcut.admin.website.model.response.CrmLeadResp;
import com.chatcut.admin.website.service.CrmLeadService;
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
import jakarta.servlet.http.HttpServletResponse;
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
 * 官网线索管理。联系方式明文返回、明文导出；终态不允许重新打开。
 * 前端 ID 统一传 String，路径不携带参数。
 */
@Tag(name = "官网线索", description = "访客线索的筛选、跟进与导出")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website/leads")
public class CrmLeadController extends BaseController {

    private final CrmLeadService crmLeadService;

    @Operation(summary = "线索筛选分页")
    @SaCheckPermission("website:lead:list")
    @GetMapping("/listCrmLeads")
    public TableDataInfo<CrmLeadResp> listCrmLeads(CrmLeadListReq request, PageQuery pageQuery) {
        return crmLeadService.pageCrmLeads(request, pageQuery);
    }

    @Operation(summary = "线索详情（含提交历史与跟进记录）")
    @SaCheckPermission("website:lead:detail")
    @GetMapping("/getCrmLeadInfo")
    public R<CrmLeadDetailResp> getCrmLeadInfo(@NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(crmLeadService.getCrmLeadInfo(WebsiteUtils.parseId(id)));
    }

    @Operation(summary = "更新线索（分配、状态/有效性/下次跟进/结果）")
    @SaCheckPermission("website:lead:update")
    @Log(title = "官网线索", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/updateCrmLead")
    public R<Void> updateCrmLead(@Validated @RequestBody CrmLeadUpdateReq request) {
        crmLeadService.updateCrmLead(WebsiteUtils.parseId(request.getId()), request);
        return R.ok();
    }

    @Operation(summary = "添加内部跟进记录（追加不可编辑/删除）")
    @SaCheckPermission("website:lead:note")
    @Log(title = "官网线索", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/addCrmLeadNote")
    public R<Void> addCrmLeadNote(@Validated @RequestBody CrmLeadNoteCreateReq request) {
        crmLeadService.addCrmLeadNote(WebsiteUtils.parseId(request.getLeadId()), request);
        return R.ok();
    }

    @Operation(summary = "按当前筛选同步导出 CSV（明文）")
    @SaCheckPermission("website:lead:export")
    @Log(title = "官网线索", businessType = BusinessType.EXPORT)
    @PostMapping("/exportCrmLeads")
    public void exportCrmLeads(CrmLeadListReq request, HttpServletResponse response) throws Exception {
        crmLeadService.exportCrmLeads(request, response);
    }
}
