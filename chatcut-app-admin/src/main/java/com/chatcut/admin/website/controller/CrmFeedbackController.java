package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.website.model.request.CrmFeedbackConvertReq;
import com.chatcut.admin.website.model.request.CrmFeedbackListReq;
import com.chatcut.admin.website.model.request.CrmFeedbackUpdateReq;
import com.chatcut.admin.website.model.response.CrmFeedbackResp;
import com.chatcut.admin.website.service.CrmFeedbackService;
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
 * 官网访客反馈管理。转线索不改变反馈状态；终态不允许重新打开。
 * 前端 ID 统一传 String，路径不携带参数。
 */
@Tag(name = "官网反馈", description = "访客反馈的处理与转线索")
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/website/feedback")
public class CrmFeedbackController extends BaseController {

    private final CrmFeedbackService crmFeedbackService;

    @Operation(summary = "反馈筛选分页")
    @SaCheckPermission("website:feedback:list")
    @GetMapping("/listCrmFeedbacks")
    public TableDataInfo<CrmFeedbackResp> listCrmFeedbacks(CrmFeedbackListReq request, PageQuery pageQuery) {
        return crmFeedbackService.pageCrmFeedbacks(request, pageQuery);
    }

    @Operation(summary = "反馈详情")
    @SaCheckPermission("website:feedback:detail")
    @GetMapping("/getCrmFeedbackInfo")
    public R<CrmFeedbackResp> getCrmFeedbackInfo(@NotBlank(message = "主键不能为空") @RequestParam("id") String id) {
        return R.ok(crmFeedbackService.getCrmFeedbackInfo(WebsiteUtils.parseId(id)));
    }

    @Operation(summary = "更新反馈（分配、状态和处理结果）")
    @SaCheckPermission("website:feedback:update")
    @Log(title = "官网反馈", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/updateCrmFeedback")
    public R<Void> updateCrmFeedback(@Validated @RequestBody CrmFeedbackUpdateReq request) {
        crmFeedbackService.updateCrmFeedback(WebsiteUtils.parseId(request.getId()), request);
        return R.ok();
    }

    @Operation(summary = "反馈转线索（同一事务完成，反馈状态保持不变）")
    @SaCheckPermission("website:feedback:convert")
    @Log(title = "官网反馈", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/convertCrmFeedbackToLead")
    public R<String> convertCrmFeedbackToLead(@Validated @RequestBody CrmFeedbackConvertReq request) {
        return R.ok("操作成功", String.valueOf(crmFeedbackService.convertCrmFeedbackToLead(WebsiteUtils.parseId(request.getId()), request)));
    }
}
