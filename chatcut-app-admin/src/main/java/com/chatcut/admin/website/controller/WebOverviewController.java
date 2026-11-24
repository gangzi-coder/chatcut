package com.chatcut.admin.website.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.system.model.req.SysOperLogReq;
import com.chatcut.admin.system.model.resp.SysOperLogResp;
import com.chatcut.admin.system.service.ISysOperLogService;
import com.chatcut.admin.website.model.response.WebOverviewResp;
import com.chatcut.admin.website.service.WebOverviewService;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.web.core.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 官网运营工作台：概览计数与操作记录。
 *
 * <p>操作记录直接复用若依操作日志（sys_oper_log），官网域写接口统一带
 * {@code @Log(title="官网内容/官网素材/官网线索/官网反馈/官网站点设置")}，按 title 筛选即可定位官网模块操作；
 * portal 访客业务事件日志在 portal 侧实现后并入展示。</p>
 */
@Tag(name = "官网工作台", description = "工作概览计数与操作记录查询")
@RequiredArgsConstructor
@RestController
@RequestMapping("/website")
public class WebOverviewController extends BaseController {

    private final WebOverviewService webOverviewService;
    private final ISysOperLogService operLogService;

    @Operation(summary = "工作概览计数（实时聚合）")
    @SaCheckPermission("website:overview:list")
    @GetMapping("/getWebOverview")
    public R<WebOverviewResp> getWebOverview() {
        return R.ok(webOverviewService.getWebOverview());
    }

    @Operation(summary = "操作记录分页（若依操作日志）",
        description = "传 title=官网内容/官网素材/官网线索/官网反馈/官网站点设置 可按模块筛选")
    @SaCheckPermission("website:logs:list")
    @GetMapping("/listOperationLogs")
    public TableDataInfo<SysOperLogResp> listOperationLogs(SysOperLogReq request, PageQuery pageQuery) {
        return operLogService.selectPageOperLogList(request, pageQuery);
    }
}
