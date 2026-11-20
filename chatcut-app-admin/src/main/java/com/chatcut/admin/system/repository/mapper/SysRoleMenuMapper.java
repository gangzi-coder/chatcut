package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.admin.system.repository.entity.SysRoleMenuEntity;

import java.util.List;

/**
 * 角色与菜单关联表 数据层
 *
 * @author Lion Li
 */
public interface SysRoleMenuMapper extends BaseMapperPlus<SysRoleMenuEntity, SysRoleMenuEntity> {

    /**
     * 根据菜单ID串删除关联关系
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    default int deleteByMenuIds(List<Long> menuIds) {
        return this.delete(new LambdaUpdateWrapper<SysRoleMenuEntity>().in(SysRoleMenuEntity::getMenuId, menuIds));
    }

}
