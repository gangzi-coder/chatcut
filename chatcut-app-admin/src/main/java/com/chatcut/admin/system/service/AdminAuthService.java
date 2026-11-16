package com.chatcut.admin.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chatcut.admin.audit.service.AuditLogService;
import com.chatcut.admin.system.model.resp.AdminProfileResp;
import com.chatcut.admin.system.repository.entity.SysAdminEntity;
import com.chatcut.admin.system.repository.mapper.AdminMapper;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.web.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public SaTokenInfo login(String username, String password) {
        SysAdminEntity admin = adminMapper.selectOne(Wrappers.<SysAdminEntity>lambdaQuery().eq(SysAdminEntity::getUsername, username));
        if (admin == null || !EnableStatus.isEnabled(admin.getStatus())
                || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            String message = "用户名或密码错误，或账号已停用";
            // 登录失败必须留痕：这是探测/暴力破解最关键的证据，此时还没有会话，只能记尝试的用户名
            recordQuietly(() -> auditLogService.recordLogin(username, false, message,
                    CommonErrorCode.UNAUTHORIZED.code()));
            throw new BizException(CommonErrorCode.UNAUTHORIZED, message);
        }
        admin.setLastLoginAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        StpAdminUtil.login(admin.getId(), false);
        recordQuietly(() -> auditLogService.recordLogin(username, true, null, null));
        return StpAdminUtil.logic().getTokenInfo();
    }

    /** 登出：先留痕再销毁会话，否则取不到操作者。 */
    public void logout() {
        Long adminId = CurrentUser.adminId();
        SysAdminEntity admin = adminId == null ? null : adminMapper.selectById(adminId);
        recordQuietly(() -> auditLogService.recordLogout(adminId, admin == null ? null : admin.getUsername()));
        StpAdminUtil.logout();
    }

    /** 审计写入失败只降级为日志，绝不能把登录/登出本身搞挂。 */
    private void recordQuietly(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("【审计-写入失败】-【原因: {}】", e.toString());
        }
    }

    public AdminProfileResp profile(Long adminId) {
        SysAdminEntity admin = adminMapper.selectById(adminId);
        if (admin == null || !EnableStatus.isEnabled(admin.getStatus())) {
            throw new BizException(CommonErrorCode.UNAUTHORIZED);
        }
        AdminProfileResp vo = new AdminProfileResp();
        vo.setId(admin.getId());
        vo.setUsername(admin.getUsername());
        vo.setDisplayName(admin.getDisplayName());
        vo.setRoles(adminMapper.selectRoleKeys(adminId, EnableStatus.ENABLED.getCode()));
        vo.setPermissions(adminMapper.selectPermissionKeys(adminId, EnableStatus.ENABLED.getCode()));
        return vo;
    }

    public boolean isEnabled(Long adminId) {
        SysAdminEntity admin = adminMapper.selectById(adminId);
        return admin != null && EnableStatus.isEnabled(admin.getStatus());
    }

    public List<String> permissions(Long adminId) {
        return adminMapper.selectPermissionKeys(adminId, EnableStatus.ENABLED.getCode());
    }
}
