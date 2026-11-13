package com.chatcut.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chatcut.admin.system.repository.mapper.PermissionMapper;
import com.chatcut.admin.system.repository.mapper.RoleMapper;
import com.chatcut.admin.system.repository.mapper.RolePermissionMapper;
import com.chatcut.admin.system.model.req.RoleSaveReq;
import com.chatcut.admin.system.repository.entity.SysPermissionEntity;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.model.resp.PermissionResp;
import com.chatcut.admin.system.model.resp.RoleDetailResp;
import com.chatcut.admin.system.model.resp.RoleResp;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /** 对外一律返回 Resp，DO 不出 repository 层。 */
    public List<RoleResp> list() {
        return roleMapper.selectList(Wrappers.<SysRoleEntity>lambdaQuery().orderByDesc(SysRoleEntity::getId))
                .stream().map(RoleService::toResp).toList();
    }

    public List<PermissionResp> permissions() {
        return permissionMapper.selectList(Wrappers.<SysPermissionEntity>lambdaQuery().orderByAsc(SysPermissionEntity::getSort))
                .stream().map(RoleService::toResp).toList();
    }

    /** 角色详情：含已勾选的权限 id 列表，前端编辑角色时回显。 */
    public RoleDetailResp detail(Long id) {
        SysRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "角色不存在");
        }
        RoleDetailResp vo = new RoleDetailResp();
        vo.setId(role.getId());
        vo.setRoleKey(role.getRoleKey());
        vo.setRoleName(role.getRoleName());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setPermissionIds(rolePermissionMapper.selectPermissionIdsByRoleId(id));
        return vo;
    }

    /** 根据角色查权限明细列表（仅启用中的权限项）。 */
    public List<PermissionResp> permissionsOfRole(Long id) {
        if (roleMapper.selectById(id) == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "角色不存在");
        }
        return rolePermissionMapper.selectPermissionsByRoleId(id)
                .stream().map(RoleService::toResp).toList();
    }

    private static RoleResp toResp(SysRoleEntity r) {
        RoleResp vo = new RoleResp();
        vo.setId(r.getId());
        vo.setRoleKey(r.getRoleKey());
        vo.setRoleName(r.getRoleName());
        vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus());
        vo.setCreatedAt(r.getCreatedAt());
        return vo;
    }

    private static PermissionResp toResp(SysPermissionEntity p) {
        PermissionResp vo = new PermissionResp();
        vo.setId(p.getId());
        vo.setParentId(p.getParentId());
        vo.setPermissionKey(p.getPermissionKey());
        vo.setPermissionName(p.getPermissionName());
        vo.setPermissionType(p.getPermissionType());
        vo.setPath(p.getPath());
        vo.setHttpMethod(p.getHttpMethod());
        vo.setSort(p.getSort());
        vo.setStatus(p.getStatus());
        return vo;
    }

    /**
     * 分布式锁在 controller 层入口加（create 按 roleKey，update/setStatus 按 id），这里只负责事务与校验。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleSaveReq req) {
        if (roleMapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleKey, req.getRoleKey())) > 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "角色标识已存在");
        }
        validatePermissions(req.getPermissionIds());
        SysRoleEntity role = new SysRoleEntity();
        role.setRoleKey(req.getRoleKey());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        role.setStatus(EnableStatus.ENABLED.getCode());
        roleMapper.insert(role);
        replacePermissions(role.getId(), req.getPermissionIds());
        return role.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, RoleSaveReq req) {
        SysRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "角色不存在");
        }
        if (roleMapper.selectCount(Wrappers.<SysRoleEntity>lambdaQuery()
                .eq(SysRoleEntity::getRoleKey, req.getRoleKey()).ne(SysRoleEntity::getId, id)) > 0) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "角色标识已存在");
        }
        validatePermissions(req.getPermissionIds());
        role.setRoleKey(req.getRoleKey());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        roleMapper.updateById(role);
        replacePermissions(id, req.getPermissionIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void setStatus(Long id, EnableStatus status) {
        SysRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        role.setStatus(status.getCode());
        roleMapper.updateById(role);
    }

    private void validatePermissions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        long count = permissionMapper.selectCount(Wrappers.<SysPermissionEntity>lambdaQuery()
                .in(SysPermissionEntity::getId, ids)
                .eq(SysPermissionEntity::getStatus, EnableStatus.ENABLED.getCode()));
        if (count != ids.stream().distinct().count()) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "存在无效权限项");
        }
    }

    private void replacePermissions(Long roleId, List<Long> ids) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (ids != null) {
            ids.stream().distinct().forEach(permissionId -> rolePermissionMapper.insert(roleId, permissionId));
        }
    }
}
