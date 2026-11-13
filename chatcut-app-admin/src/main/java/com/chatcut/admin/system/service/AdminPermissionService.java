package com.chatcut.admin.system.service;

import cn.dev33.satoken.stp.StpInterface;
import com.chatcut.common.security.LoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminPermissionService implements StpInterface {

    private final AdminAuthService authService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (!isAdminLogin(loginId, loginType)) {
            return List.of();
        }
        return authService.permissions(Long.valueOf(loginId.toString()));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (!isAdminLogin(loginId, loginType)) {
            return List.of();
        }
        return authService.profile(Long.valueOf(loginId.toString())).getRoles();
    }

    /** 只处理 admin 体系且账号处于启用状态的登录，其它一律返回空权限。 */
    private boolean isAdminLogin(Object loginId, String loginType) {
        return LoginType.ADMIN.value().equals(loginType)
                && authService.isEnabled(Long.valueOf(loginId.toString()));
    }
}
