package com.chatcut.admin.system.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.chatcut.admin.system.model.req.AdminLoginReq;
import com.chatcut.admin.system.model.resp.AdminProfileResp;
import com.chatcut.admin.system.service.AdminAuthService;
import com.chatcut.common.core.domain.Result;
import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.web.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证", description = "管理员登录/登出/当前用户")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {
    private final AdminAuthService authService;

    @Operation(summary = "登录", description = "用返回的 tokenValue 放入 Authorize（Bearer），后续接口即可联调")
    @PostMapping("/login")
    public Result<SaTokenInfo> login(@Valid @RequestBody AdminLoginReq req) {
        return Result.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpAdminUtil.logout();
        return Result.ok();
    }

    @Operation(summary = "当前登录管理员信息", description = "含角色 key 与权限 key 列表")
    @GetMapping("/me")
    public Result<AdminProfileResp> me() {
        log.info("Current admin id: {}", CurrentUser.adminId());
        return Result.ok(authService.profile(CurrentUser.adminId()));
    }
}
