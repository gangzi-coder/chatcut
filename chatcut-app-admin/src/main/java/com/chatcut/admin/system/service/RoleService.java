package com.chatcut.admin.system.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chatcut.admin.audit.support.AuditContext;
import com.chatcut.admin.system.model.req.RoleSaveReq;
import com.chatcut.admin.system.model.resp.PermissionResp;
import com.chatcut.admin.system.model.resp.RoleDetailResp;
import com.chatcut.admin.system.model.resp.RoleResp;
import com.chatcut.admin.system.repository.entity.SysPermissionEntity;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.repository.mapper.PermissionMapper;
import com.chatcut.admin.system.repository.mapper.RoleMapper;
import com.chatcut.admin.system.repository.mapper.RolePermissionMapper;
import com.chatcut.common.core.enums.EnableStatus;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        long permissionCount = req.getPermissionIds() == null ? 0 : req.getPermissionIds().stream().distinct().count();
        AuditContext.target("role", role.getId(), role.getRoleName());
        AuditContext.captureAfter(role);
        AuditContext.content("创建角色 " + role.getRoleName() + "（权限 " + permissionCount + " 项）");
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

        // 审计：先取旧权限集与旧字段快照，再改；改完补 after 并算 diff
        AuditContext.target("role", id, role.getRoleName());
        List<Long> beforePermissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(id);
        AuditContext.captureBefore(role);

        role.setRoleKey(req.getRoleKey());
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        roleMapper.updateById(role);
        replacePermissions(id, req.getPermissionIds());

        List<Long> afterPermissionIds = req.getPermissionIds() == null ? List.of()
                : req.getPermissionIds().stream().distinct().toList();
        AuditContext.captureAfter(role);
        // 对象名同步成改名后的值，与摘要保持一致；旧名在 roleName 的变更明细里
        AuditContext.target("role", role.getId(), role.getRoleName());
        AuditContext.collectionChange("permissions", "权限", beforePermissionIds, afterPermissionIds,
                permissionNames(Stream.concat(beforePermissionIds.stream(), afterPermissionIds.stream()).toList()));

        long added = afterPermissionIds.stream().filter(pid -> !beforePermissionIds.contains(pid)).count();
        long removed = beforePermissionIds.stream().filter(pid -> !afterPermissionIds.contains(pid)).count();
        String content = "编辑角色 " + role.getRoleName()
                + (added == 0 && removed == 0 ? "" : "（权限 +" + added + "/-" + removed + "）");
        AuditContext.content(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setStatus(Long id, EnableStatus status) {
        AuditContext.target("role", id, null);
        SysRoleEntity role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        AuditContext.target("role", role.getId(), role.getRoleName());
        AuditContext.captureBefore(role);
        role.setStatus(status.getCode());
        roleMapper.updateById(role);
        AuditContext.captureAfter(role);
        AuditContext.content((status == EnableStatus.ENABLED ? "启用角色 " : "停用角色 ") + role.getRoleName());
    }

    /** 权限 id -> 权限名，供审计明细展示；查不到时展示 id。 */
    private Map<Long, String> permissionNames(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return permissionMapper.selectBatchIds(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(SysPermissionEntity::getId, SysPermissionEntity::getPermissionName,
                        (a, b) -> a));
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
