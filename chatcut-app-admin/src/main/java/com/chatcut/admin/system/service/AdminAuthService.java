package com.chatcut.admin.system.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chatcut.admin.system.repository.mapper.AdminMapper;
import com.chatcut.admin.system.repository.entity.SysAdminEntity;
import com.chatcut.admin.system.model.resp.AdminProfileResp;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SaTokenInfo login(String username, String password) {
        SysAdminEntity admin = adminMapper.selectOne(Wrappers.<SysAdminEntity>lambdaQuery().eq(SysAdminEntity::getUsername, username));
        if (admin == null || !EnableStatus.isEnabled(admin.getStatus())
                || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BizException(CommonErrorCode.UNAUTHORIZED, "用户名或密码错误，或账号已停用");
        }
        admin.setLastLoginAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        StpAdminUtil.login(admin.getId(), false);
        return StpAdminUtil.logic().getTokenInfo();
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
