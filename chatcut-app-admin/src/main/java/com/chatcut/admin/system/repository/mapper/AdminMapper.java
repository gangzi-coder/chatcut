package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatcut.admin.system.repository.entity.SysAdminEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminMapper extends BaseMapper<SysAdminEntity> {

    /** 管理员已启用的角色 key（status 由调用方传 EnableStatus 的 code，避免 SQL 里写死 0/1）。 */
    @Select("""
            SELECT r.role_key
            FROM sys_admin_role ar
            JOIN sys_role r ON r.id = ar.role_id AND r.deleted = 0 AND r.status = #{status}
            JOIN sys_admin a ON a.id = ar.admin_id AND a.deleted = 0 AND a.status = #{status}
            WHERE ar.admin_id = #{adminId}
            """)
    List<String> selectRoleKeys(@Param("adminId") Long adminId, @Param("status") int status);

    /** 管理员已启用的权限 key（同上，status 走参数）。 */
    @Select("""
            SELECT DISTINCT p.permission_key
            FROM sys_admin_role ar
            JOIN sys_admin a ON a.id = ar.admin_id AND a.deleted = 0 AND a.status = #{status}
            JOIN sys_role r ON r.id = ar.role_id AND r.deleted = 0 AND r.status = #{status}
            JOIN sys_role_permission rp ON rp.role_id = r.id
            JOIN sys_permission p ON p.id = rp.permission_id AND p.deleted = 0 AND p.status = #{status}
            WHERE ar.admin_id = #{adminId}
            """)
    List<String> selectPermissionKeys(@Param("adminId") Long adminId, @Param("status") int status);
}
