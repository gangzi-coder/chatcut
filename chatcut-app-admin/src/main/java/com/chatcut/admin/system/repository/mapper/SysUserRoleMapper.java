package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.admin.system.repository.entity.SysUserRoleEntity;

import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author Lion Li
 */
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRoleEntity, SysUserRoleEntity> {

    /**
     * 根据角色ID查询关联的用户ID列表
     *
     * @param roleId 角色ID
     * @return 关联到指定角色的用户ID列表
     */
    default List<Long> selectUserIdsByRoleId(Long roleId) {
        return this.selectObjs(new LambdaQueryWrapper<SysUserRoleEntity>()
            .select(SysUserRoleEntity::getUserId).eq(SysUserRoleEntity::getRoleId, roleId)
        );
    }

}
