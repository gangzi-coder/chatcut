package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import com.chatcut.common.mybatis.annotation.DataColumn;
import com.chatcut.common.mybatis.annotation.DataPermission;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.model.resp.SysRoleResp;

import java.util.List;

/**
 * 角色表 数据层
 *
 * @author Lion Li
 */
public interface SysRoleMapper extends BaseMapperPlus<SysRoleEntity, SysRoleResp> {

    /**
     * 构建根据用户ID查询角色ID的SQL子查询
     *
     * @param userId 用户ID
     * @return 查询用户对应角色ID的SQL语句字符串
     */
    default String buildRoleByUserSql(Long userId) {
        return """
                select role_id from sys_user_role where user_id = %d
            """.formatted(userId);
    }

    /**
     * 分页查询角色列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 包含角色信息的分页结果
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default Page<SysRoleResp> selectPageRoleList(@Param("page") Page<SysRoleEntity> page, @Param(Constants.WRAPPER) Wrapper<SysRoleEntity> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 根据条件查询角色数据
     *
     * @param queryWrapper 查询条件
     * @return 角色数据集合信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default List<SysRoleResp> selectRoleList(@Param(Constants.WRAPPER) Wrapper<SysRoleEntity> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据角色ID集合查询角色数量
     *
     * @param roleIds 角色ID列表
     * @return 匹配的角色数量
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default long selectRoleCount(List<Long> roleIds) {
        return this.selectCount(new LambdaQueryWrapper<SysRoleEntity>().in(SysRoleEntity::getRoleId, roleIds));
    }

    /**
     * 根据角色ID查询角色信息
     *
     * @param roleId 角色ID
     * @return 对应的角色信息
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "create_dept"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default SysRoleResp selectRoleById(Long roleId) {
        return this.selectVoById(roleId);
    }

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    default List<SysRoleResp> selectRolesByUserId(Long userId) {
        return this.selectVoList(new LambdaQueryWrapper<SysRoleEntity>()
            .select(SysRoleEntity::getRoleId, SysRoleEntity::getRoleName, SysRoleEntity::getRoleKey,
                SysRoleEntity::getRoleSort, SysRoleEntity::getDataScope, SysRoleEntity::getStatus)
            .inSql(SysRoleEntity::getRoleId, this.buildRoleByUserSql(userId)));
    }

}
