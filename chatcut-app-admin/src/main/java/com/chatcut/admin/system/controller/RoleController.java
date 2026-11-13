package com.chatcut.admin.system.controller;

import com.chatcut.admin.system.model.req.RoleSaveReq;
import com.chatcut.admin.system.model.resp.PermissionResp;
import com.chatcut.admin.system.model.resp.RoleDetailResp;
import com.chatcut.admin.system.model.resp.RoleResp;
import com.chatcut.admin.system.service.RoleService;
import com.chatcut.common.core.domain.Result;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.redis.lock.DistributedLock;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "角色与权限", description = "角色 CRUD、权限查询、角色-权限绑定")
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final DistributedLock distributedLock;

    @Operation(summary = "角色列表", description = "全量角色（管理端角色数量少，不分页）")
    @GetMapping("/roles")
    @SaCheckPermission(value = "system:role:list", type = "admin")
    public Result<List<RoleResp>> roles() {
        return Result.ok(roleService.list());
    }

    @Operation(summary = "角色详情", description = "含该角色已勾选的权限 id 列表（编辑回显用）")
    @GetMapping("/roles/{id}")
    @SaCheckPermission(value = "system:role:detail", type = "admin")
    public Result<RoleDetailResp> roleDetail(@Parameter(description = "角色 id") @PathVariable Long id) {
        return Result.ok(roleService.detail(id));
    }

    @Operation(summary = "根据角色查权限列表", description = "返回该角色绑定的权限明细（仅启用中的权限项）")
    @GetMapping("/roles/{id}/permissions")
    @SaCheckPermission(value = "system:role:permission:list", type = "admin")
    public Result<List<PermissionResp>> rolePermissions(@Parameter(description = "角色 id") @PathVariable Long id) {
        return Result.ok(roleService.permissionsOfRole(id));
    }

    @Operation(summary = "创建角色", description = "按 roleKey 加分布式锁，防止并发创建同一标识")
    @PostMapping("/roles")
    @SaCheckPermission(value = "system:role:create", type = "admin")
    public Result<Map<String, Long>> createRole(@Valid @RequestBody RoleSaveReq req) {
        Long id = distributedLock.execute("role:create", req.getRoleKey(),
                () -> roleService.create(req));
        return Result.ok(Map.of("id", id));
    }

    @Operation(summary = "更新角色", description = "全量更新字段与权限绑定；按角色 id 加锁，与启停接口互斥")
    @PostMapping("/roles/{id}")
    @SaCheckPermission(value = "system:role:update", type = "admin")
    public Result<Void> updateRole(@Parameter(description = "角色 id") @PathVariable Long id, @Valid @RequestBody RoleSaveReq req) {
        distributedLock.execute("role:write", id, () -> {
            roleService.update(id, req);
            return null;
        });
        return Result.ok();
    }

    @Operation(summary = "启用/停用角色", description = "status: 1=启用 0=停用；按角色 id 加锁，与更新接口互斥")
    @PostMapping("/roles/{id}/status")
    @SaCheckPermission(value = "system:role:status", type = "admin")
    public Result<Void> status(@Parameter(description = "角色 id") @PathVariable Long id,
                               @Parameter(description = "目标状态：1=启用 0=停用") @RequestParam int status) {
        EnableStatus targetStatus = EnableStatus.of(status);
        distributedLock.execute("role:write", id, () -> {
            roleService.setStatus(id, targetStatus);
            return null;
        });
        return Result.ok();
    }

    @Operation(summary = "权限项列表", description = "全量权限定义，创建/编辑角色时的可勾选项")
    @GetMapping("/permissions")
    @SaCheckPermission(value = "system:permission:list", type = "admin")
    public Result<List<PermissionResp>> permissions() {
        return Result.ok(roleService.permissions());
    }
}
