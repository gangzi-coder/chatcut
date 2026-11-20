package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.utils.StreamUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.admin.system.repository.entity.SysMenuEntity;
import com.chatcut.admin.system.model.resp.SysMenuResp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 菜单表 数据层
 *
 * @author Lion Li
 */
public interface SysMenuMapper extends BaseMapperPlus<SysMenuEntity, SysMenuResp> {

    /**
     * 构建用户权限菜单 SQL
     *
     * <p>
     * 查询用户所属角色所拥有的菜单权限，用于权限判断、菜单加载等场景
     * </p>
     *
     * @param userId 用户ID
     * @return SQL 字符串，用于 inSql 条件
     */
    default String buildMenuByUserSql(Long userId) {
        return """
                select menu_id from sys_role_menu where role_id in (
                    select sur.role_id from sys_user_role sur
                        left join sys_role sr on sr.role_id = sur.role_id
                        where sur.user_id = %d and sr.status = '0'
                )
            """.formatted(userId);
    }

    /**
     * 构建角色对应的菜单ID SQL 子查询
     *
     * <p>
     * 用于根据角色ID查询其所拥有的菜单权限（用于权限标识、菜单显示等场景）
     * 通常配合 inSql 使用
     * </p>
     *
     * @param roleId 角色ID
     * @return 查询菜单ID的 SQL 子查询字符串
     */
    default String buildMenuByRoleSql(Long roleId) {
        return """
                select srm.menu_id from sys_role_menu srm
                    left join sys_role sr on sr.role_id = srm.role_id
                    where srm.role_id = %d and sr.status = '0'
            """.formatted(roleId);
    }

    /**
     * 构建角色所关联菜单的父菜单ID查询 SQL
     *
     * <p>
     * 用于配合菜单勾选树结构的 {@code menuCheckStrictly} 模式，过滤掉非叶子节点（父菜单），
     * 只返回角色实际勾选的末级菜单
     * </p>
     *
     * @param roleId 角色ID
     * @return SQL 语句字符串（查询菜单的父菜单ID）
     */
    default String buildParentMenuByRoleSql(Long roleId) {
        return """
                select parent_id from sys_menu where menu_id in (
                    select srm.menu_id from sys_role_menu srm
                        left join sys_role sr on sr.role_id = srm.role_id
                        where srm.role_id = %d and sr.status = '0'
                )
            """.formatted(roleId);
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    default Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> list = this.selectObjs(
            new LambdaQueryWrapper<SysMenuEntity>()
                .select(SysMenuEntity::getPerms)
                .inSql(SysMenuEntity::getMenuId, this.buildMenuByUserSql(userId))
                .isNotNull(SysMenuEntity::getPerms)
        );
        return new HashSet<>(StreamUtils.filter(list, StringUtils::isNotBlank));
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    default Set<String> selectMenuPermsByRoleId(Long roleId) {
        List<String> list = this.selectObjs(
            new LambdaQueryWrapper<SysMenuEntity>()
                .select(SysMenuEntity::getPerms)
                .inSql(SysMenuEntity::getMenuId, this.buildMenuByRoleSql(roleId))
                .isNotNull(SysMenuEntity::getPerms)
        );
        return new HashSet<>(StreamUtils.filter(list, StringUtils::isNotBlank));
    }

    /**
     * 根据用户ID查询菜单
     *
     * @return 菜单列表
     */
    default List<SysMenuEntity> selectMenuTreeAll() {
        LambdaQueryWrapper<SysMenuEntity> lqw = new LambdaQueryWrapper<SysMenuEntity>()
            .in(SysMenuEntity::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
            .eq(SysMenuEntity::getStatus, SystemConstants.NORMAL)
            .orderByAsc(SysMenuEntity::getParentId)
            .orderByAsc(SysMenuEntity::getOrderNum);
        return this.selectList(lqw);
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId            角色ID
     * @param menuCheckStrictly 菜单树选择项是否关联显示
     * @return 选中菜单列表
     */
    default List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysMenuEntity::getMenuId)
            .inSql(SysMenuEntity::getMenuId, buildMenuByRoleSql(roleId))
            .orderByAsc(SysMenuEntity::getParentId)
            .orderByAsc(SysMenuEntity::getOrderNum);
        if (menuCheckStrictly) {
            wrapper.notInSql(SysMenuEntity::getMenuId, this.buildParentMenuByRoleSql(roleId));
        }
        return this.selectObjs(wrapper);
    }

}
