package com.chatcut.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.admin.system.repository.mapper.AdminMapper;
import com.chatcut.admin.system.repository.mapper.AdminRoleMapper;
import com.chatcut.admin.system.repository.mapper.RoleMapper;
import com.chatcut.admin.system.model.req.AdminCreateReq;
import com.chatcut.admin.system.repository.entity.SysAdminEntity;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.model.resp.AdminListResp;
import com.chatcut.common.core.domain.PageResult;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAccountService {
    private final AdminMapper adminMapper;
    private final RoleMapper roleMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /** 分布式锁在 controller 层入口加（按 username 粒度），这里只负责事务与校验。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(AdminCreateReq req) {
        if (adminMapper.selectCount(Wrappers.<SysAdminEntity>lambdaQuery().eq(SysAdminEntity::getUsername, req.getUsername())) > 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "用户名已存在");
        }
        List<Long> roleIds = req.getRoleIds() == null ? List.of() : req.getRoleIds().stream().distinct().toList();
        for (Long roleId : roleIds) {
            SysRoleEntity role = roleMapper.selectById(roleId);
            if (role == null || !EnableStatus.isEnabled(role.getStatus())) {
                throw new BizException(CommonErrorCode.NOT_FOUND, "角色不存在或已停用");
            }
        }
        SysAdminEntity admin = new SysAdminEntity();
        admin.setUsername(req.getUsername());
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        admin.setDisplayName(req.getDisplayName());
        admin.setStatus(EnableStatus.ENABLED.getCode());
        adminMapper.insert(admin);
        roleIds.forEach(roleId -> adminRoleMapper.insert(admin.getId(), roleId));
        return admin.getId();
    }

    /** 管理员分页列表：keyword 模糊匹配用户名/显示名，附带每个管理员的角色。 */
    public PageResult<AdminListResp> page(long pageNo, long pageSize, String keyword) {
        Page<SysAdminEntity> page = adminMapper.selectPage(new Page<>(pageNo, pageSize),
                Wrappers.<SysAdminEntity>lambdaQuery()
                        .and(StringUtils.hasText(keyword),
                                w -> w.like(SysAdminEntity::getUsername, keyword).or().like(SysAdminEntity::getDisplayName, keyword))
                        .orderByDesc(SysAdminEntity::getId));
        List<AdminListResp> list = page.getRecords().stream().map(this::toResp).toList();
        return PageResult.of(list, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /** 管理员详情（含角色）。 */
    public AdminListResp detail(Long id) {
        SysAdminEntity admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "管理员不存在");
        }
        return toResp(admin);
    }

    /** 启用/停用管理员。锁在 controller 入口；停用自己直接拒绝。 */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, EnableStatus status, Long operatorId) {
        if (id.equals(operatorId) && status == EnableStatus.DISABLED) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "不能停用当前登录账号");
        }
        SysAdminEntity admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "管理员不存在");
        }
        admin.setStatus(status.getCode());
        adminMapper.updateById(admin);
    }

    private AdminListResp toResp(SysAdminEntity a) {
        AdminListResp vo = new AdminListResp();
        vo.setId(a.getId());
        vo.setUsername(a.getUsername());
        vo.setDisplayName(a.getDisplayName());
        vo.setPhone(a.getPhone());
        vo.setStatus(a.getStatus());
        vo.setLastLoginAt(a.getLastLoginAt());
        vo.setRoles(adminRoleMapper.selectRolesByAdminId(a.getId()));
        return vo;
    }
}
