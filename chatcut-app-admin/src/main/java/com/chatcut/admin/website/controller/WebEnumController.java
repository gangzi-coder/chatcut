package com.chatcut.admin.website.controller;

import com.chatcut.admin.website.model.response.WebEnumItemResp;
import com.chatcut.admin.website.service.WebEnumService;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 官网公共枚举查询。仅登录校验，不做权限控制，供前端下拉选择。
 */
@Tag(name = "官网枚举", description = "官网域公共枚举选项查询（无权限控制）")
@RequiredArgsConstructor
@RestController
@RequestMapping("/website")
public class WebEnumController extends BaseController {

    private final WebEnumService webEnumService;

    @Operation(summary = "全部官网枚举选项",
        description = "返回 {枚举组名: [{value,label}]}，组名如 ContentType、LeadStatus、FeedbackStatus 等 13 组")
    @GetMapping("/getWebEnums")
    public R<Map<String, List<WebEnumItemResp>>> getWebEnums() {
        return R.ok(webEnumService.getWebEnums());
    }
}
