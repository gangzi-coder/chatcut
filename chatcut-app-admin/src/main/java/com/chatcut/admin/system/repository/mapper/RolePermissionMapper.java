package com.chatcut.admin.system.repository.mapper;

import com.chatcut.admin.system.repository.entity.SysPermissionEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RolePermissionMapper {
    @Delete("DELETE FROM sys_role_permission WHERE role_id=#{roleId}") int deleteByRoleId(Long roleId);
    @Insert("INSERT INTO sys_role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})") int insert(Long roleId, Long permissionId);

    /** 某角色已勾选的权限 id（角色详情回显用）。 */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(Long roleId);

    /** 根据角色查权限明细列表（仅启用中的权限项）。 */
    @Select("""
            SELECT p.*
            FROM sys_role_permission rp
            JOIN sys_permission p ON p.id = rp.permission_id AND p.deleted = 0
            WHERE rp.role_id = #{roleId}
            ORDER BY p.sort, p.id
            """)
    List<SysPermissionEntity> selectPermissionsByRoleId(Long roleId);
}
