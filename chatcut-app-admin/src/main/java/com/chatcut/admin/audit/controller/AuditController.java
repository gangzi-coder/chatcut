package com.chatcut.admin.audit.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.admin.audit.model.req.AuditLogQueryReq;
import com.chatcut.admin.audit.model.resp.AuditLogDetailResp;
import com.chatcut.admin.audit.model.resp.AuditLogResp;
import com.chatcut.admin.audit.service.AuditQueryService;
import com.chatcut.common.core.domain.PageResult;
import com.chatcut.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志查询。
 *
 * <p>只读接口：没有新增、修改、删除。审计记录的删除只能走 DBA 审批 + 保留期归档流程。</p>
 */
@Tag(name = "审计日志", description = "管理端操作留痕的查询（只读）")
@RestController
@RequestMapping("/system/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditQueryService auditQueryService;

    @Operation(summary = "审计日志分页列表", description = "支持按操作者/模块/动作/对象/结果/时间范围筛选")
    @GetMapping
    @SaCheckPermission(value = "system:audit:list", type = "admin")
    public Result<PageResult<AuditLogResp>> page(AuditLogQueryReq req) {
        return Result.ok(auditQueryService.page(req));
    }

    @Operation(summary = "审计日志详情", description = "含入参快照、前后快照与字段级变更明细")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "system:audit:detail", type = "admin")
    public Result<AuditLogDetailResp> detail(@Parameter(description = "审计记录 id") @PathVariable Long id) {
        return Result.ok(auditQueryService.detail(id));
    }
}
