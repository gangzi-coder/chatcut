package com.chatcut.admin.system.repository.mapper;

import com.chatcut.admin.system.model.resp.RoleBriefResp;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminRoleMapper {
    @Delete("DELETE FROM sys_admin_role WHERE admin_id=#{adminId}") int deleteByAdminId(Long adminId);
    @Insert("INSERT INTO sys_admin_role(admin_id, role_id) VALUES(#{adminId}, #{roleId})") int insert(Long adminId, Long roleId);

    /** 某管理员拥有的角色简要信息（含停用角色，便于列表如实展示）。 */
    @Select("""
            SELECT r.id, r.role_key AS roleKey, r.role_name AS roleName
            FROM sys_admin_role ar
            JOIN sys_role r ON r.id = ar.role_id AND r.deleted = 0
            WHERE ar.admin_id = #{adminId}
            ORDER BY r.id
            """)
    List<RoleBriefResp> selectRolesByAdminId(Long adminId);
}
