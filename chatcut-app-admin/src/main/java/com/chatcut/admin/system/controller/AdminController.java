package com.chatcut.admin.system.controller;

import com.chatcut.admin.system.model.req.AdminCreateReq;
import com.chatcut.admin.system.model.resp.AdminListResp;
import com.chatcut.admin.system.service.AdminAccountService;
import com.chatcut.common.core.domain.PageResult;
import com.chatcut.common.core.domain.Result;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.redis.lock.DistributedLock;
import com.chatcut.common.security.web.CurrentUser;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理员管理", description = "管理员的创建、查询、启停")
@RestController
@RequestMapping("/system/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminAccountService accountService;
    private final DistributedLock distributedLock;

    @Operation(summary = "创建管理员", description = "按 username 加分布式锁，防止并发创建同名账号")
    @PostMapping
    @SaCheckPermission(value = "system:admin:create", type = "admin")
    public Result<Map<String, Long>> create(@Valid @RequestBody AdminCreateReq req) {
        Long id = distributedLock.execute("admin:create", req.getUsername(),
                () -> accountService.create(req));
        return Result.ok(Map.of("id", id));
    }

    @Operation(summary = "管理员分页列表", description = "keyword 模糊匹配用户名/显示名，附带每个管理员拥有的角色")
    @GetMapping
    @SaCheckPermission(value = "system:admin:list", type = "admin")
    public Result<PageResult<AdminListResp>> page(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") long pageNo,
            @Parameter(description = "每页条数，上限 200") @RequestParam(defaultValue = "10") long pageSize,
            @Parameter(description = "模糊匹配用户名/显示名，可空") @RequestParam(required = false) String keyword) {
        return Result.ok(accountService.page(pageNo, pageSize, keyword));
    }

    @Operation(summary = "管理员详情", description = "含角色列表")
    @GetMapping("/{id}")
    @SaCheckPermission(value = "system:admin:detail", type = "admin")
    public Result<AdminListResp> detail(@Parameter(description = "管理员 id") @PathVariable Long id) {
        return Result.ok(accountService.detail(id));
    }

    @Operation(summary = "启用/停用管理员", description = "status: 1=启用 0=停用；不能停用当前登录账号")
    @PostMapping("/{id}/status")
    @SaCheckPermission(value = "system:admin:status", type = "admin")
    public Result<Void> updateStatus(@Parameter(description = "管理员 id") @PathVariable Long id,
                                     @Parameter(description = "目标状态：1=启用 0=停用") @RequestParam int status) {
        EnableStatus targetStatus = EnableStatus.of(status);
        distributedLock.execute("admin:write", id, () -> {
            accountService.updateStatus(id, targetStatus, CurrentUser.adminId());
            return null;
        });
        return Result.ok();
    }
}
