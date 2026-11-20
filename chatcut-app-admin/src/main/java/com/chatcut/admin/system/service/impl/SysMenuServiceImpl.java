package com.chatcut.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.Constants;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StreamUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.TreeBuildUtils;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.admin.system.repository.entity.SysMenuEntity;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.repository.entity.SysRoleMenuEntity;
import com.chatcut.admin.system.model.req.SysMenuReq;
import com.chatcut.admin.system.model.resp.MetaResp;
import com.chatcut.admin.system.model.resp.RouterResp;
import com.chatcut.admin.system.model.resp.SysMenuResp;
import com.chatcut.admin.system.repository.mapper.SysMenuMapper;
import com.chatcut.admin.system.repository.mapper.SysRoleMapper;
import com.chatcut.admin.system.repository.mapper.SysRoleMenuMapper;
import com.chatcut.admin.system.service.ISysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 菜单 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysMenuServiceImpl implements ISysMenuService {

    private final SysMenuMapper baseMapper;
    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 根据用户查询系统菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuResp> selectMenuList(Long userId) {
        return selectMenuList(new SysMenuReq(), userId);
    }

    /**
     * 查询系统菜单列表
     *
     * @param menu 菜单信息
     * @return 菜单列表
     */
    @Override
    public List<SysMenuResp> selectMenuList(SysMenuReq menu, Long userId) {
        List<SysMenuResp> menuList;
        LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<>();
        // 管理员显示所有菜单信息 不是管理员 按用户id过滤菜单
        if (!LoginHelper.isSuperAdmin(userId)) {
            // 通过用户id获取角色id 通过角色id获取菜单id 然后in菜单
            wrapper.inSql(SysMenuEntity::getMenuId, baseMapper.buildMenuByUserSql(userId));
        }
        menuList = baseMapper.selectVoList(
            wrapper.like(StringUtils.isNotBlank(menu.getMenuName()), SysMenuEntity::getMenuName, menu.getMenuName())
                .eq(StringUtils.isNotBlank(menu.getVisible()), SysMenuEntity::getVisible, menu.getVisible())
                .eq(StringUtils.isNotBlank(menu.getStatus()), SysMenuEntity::getStatus, menu.getStatus())
                .eq(StringUtils.isNotBlank(menu.getMenuType()), SysMenuEntity::getMenuType, menu.getMenuType())
                .eq(ObjectUtil.isNotNull(menu.getParentId()), SysMenuEntity::getParentId, menu.getParentId())
                .orderByAsc(SysMenuEntity::getParentId)
                .orderByAsc(SysMenuEntity::getOrderNum));
        return menuList;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        return baseMapper.selectMenuPermsByUserId(userId);
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId) {
        return baseMapper.selectMenuPermsByRoleId(roleId);
    }

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    public List<SysMenuEntity> selectMenuTreeByUserId(Long userId) {
        List<SysMenuEntity> menus;
        if (LoginHelper.isSuperAdmin(userId)) {
            menus = baseMapper.selectMenuTreeAll();
        } else {
            LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<>();
            menus = baseMapper.selectList(
                wrapper.in(SysMenuEntity::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
                    .eq(SysMenuEntity::getStatus, SystemConstants.NORMAL)
                    .inSql(SysMenuEntity::getMenuId, baseMapper.buildMenuByUserSql(userId))
                    .orderByAsc(SysMenuEntity::getParentId)
                    .orderByAsc(SysMenuEntity::getOrderNum));
        }
        return getChildPerms(menus, Constants.TOP_PARENT_ID);
    }

    /**
     * 根据角色ID查询菜单树信息
     *
     * @param roleId 角色ID
     * @return 选中菜单列表
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId) {
        SysRoleEntity role = roleMapper.selectById(roleId);
        return baseMapper.selectMenuListByRoleId(roleId, role.getMenuCheckStrictly());
    }



    /**
     * 构建前端路由所需要的菜单
     * 路由name命名规则 path首字母转大写 + id
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterResp> buildMenus(List<SysMenuEntity> menus) {
        List<RouterResp> routers = new LinkedList<>();
        for (SysMenuEntity menu : menus) {
            String name = menu.getRouteName() + menu.getMenuId();
            RouterResp router = new RouterResp();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(name);
            router.setPath(menu.getRouterPath());
            router.setComponent(menu.getComponentInfo());
            router.setQuery(menu.getQueryParam());
            router.setMeta(new MetaResp(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath(), menu.getRemark()));
            List<SysMenuEntity> cMenus = menu.getChildren();
            if (CollUtil.isNotEmpty(cMenus) && SystemConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (menu.isMenuFrame()) {
                String frameName = StringUtils.capitalize(menu.getPath()) + menu.getMenuId();
                router.setMeta(null);
                List<RouterResp> childrenList = new ArrayList<>();
                RouterResp children = new RouterResp();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(frameName);
                children.setMeta(new MetaResp(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath(), menu.getRemark()));
                children.setQuery(menu.getQueryParam());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().equals(Constants.TOP_PARENT_ID) && menu.isInnerLink()) {
                router.setMeta(new MetaResp(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterResp> childrenList = new ArrayList<>();
                RouterResp children = new RouterResp();
                String routerPath = SysMenuEntity.innerLinkReplaceEach(menu.getPath());
                String innerLinkName = StringUtils.capitalize(routerPath) + menu.getMenuId();
                children.setPath(routerPath);
                children.setComponent(SystemConstants.INNER_LINK);
                children.setName(innerLinkName);
                children.setMeta(new MetaResp(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param menus 菜单列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildMenuTreeSelect(List<SysMenuResp> menus) {
        if (CollUtil.isEmpty(menus)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.build(menus, (menu, tree) -> {
            Tree<Long> menuTree = tree.setId(menu.getMenuId())
                .setParentId(menu.getParentId())
                .setName(menu.getMenuName())
                .setWeight(menu.getOrderNum());
            menuTree.put("menuType", menu.getMenuType());
            menuTree.put("icon", menu.getIcon());
            menuTree.put("visible", menu.getVisible());
            menuTree.put("status", menu.getStatus());
        });
    }

    /**
     * 根据菜单ID查询信息
     *
     * @param menuId 菜单ID
     * @return 菜单信息
     */
    @Override
    public SysMenuResp selectMenuById(Long menuId) {
        return baseMapper.selectVoById(menuId);
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(Long menuId) {
        return baseMapper.exists(new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getParentId, menuId));
    }

    /**
     * 是否存在菜单子节点
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    @Override
    public boolean hasChildByMenuId(List<Long> menuIds) {
        return baseMapper.exists(new LambdaQueryWrapper<SysMenuEntity>().in(SysMenuEntity::getParentId, menuIds).notIn(SysMenuEntity::getMenuId, menuIds));
    }

    /**
     * 查询菜单使用数量
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public boolean checkMenuExistRole(Long menuId) {
        return roleMenuMapper.exists(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getMenuId, menuId));
    }

    /**
     * 新增保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    public int insertMenu(SysMenuReq bo) {
        SysMenuEntity menu = MapstructUtils.convert(bo, SysMenuEntity.class);
        return baseMapper.insert(menu);
    }

    /**
     * 修改保存菜单信息
     *
     * @param bo 菜单信息
     * @return 结果
     */
    @Override
    public int updateMenu(SysMenuReq bo) {
        SysMenuEntity menu = MapstructUtils.convert(bo, SysMenuEntity.class);
        return baseMapper.updateById(menu);
    }

    /**
     * 删除菜单管理信息
     *
     * @param menuId 菜单ID
     * @return 结果
     */
    @Override
    public int deleteMenuById(Long menuId) {
        return baseMapper.deleteById(menuId);
    }

    /**
     * 批量删除菜单管理信息
     *
     * @param menuIds 菜单ID串
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenuById(List<Long> menuIds) {
        baseMapper.deleteByIds(menuIds);
        roleMenuMapper.deleteByMenuIds(menuIds);
    }

    /**
     * 校验菜单名称是否唯一
     *
     * @param menu 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkMenuNameUnique(SysMenuReq menu) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getMenuName, menu.getMenuName())
            .eq(SysMenuEntity::getParentId, menu.getParentId())
            .ne(ObjectUtil.isNotNull(menu.getMenuId()), SysMenuEntity::getMenuId, menu.getMenuId()));
        return !exist;
    }

    /**
     * 校验路由名称是否唯一
     *
     * @param menuBo 菜单信息
     * @return 结果
     */
    @Override
    public boolean checkRouteConfigUnique(SysMenuReq menuBo) {
        SysMenuEntity menu = MapstructUtils.convert(menuBo, SysMenuEntity.class);
        if (SystemConstants.TYPE_BUTTON.equals(menu.getMenuType())) {
            return true;
        }
        long menuId = ObjectUtil.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        Long parentId = menu.getParentId();
        String path = menu.getPath();
        String routeName = StringUtils.isEmpty(menu.getRouteName()) ? path : menu.getRouteName();
        List<SysMenuEntity> sysMenuList = baseMapper.selectList(
            new LambdaQueryWrapper<SysMenuEntity>()
                .in(SysMenuEntity::getMenuType, SystemConstants.TYPE_DIR, SystemConstants.TYPE_MENU)
                .and(w ->
                    w.eq(SysMenuEntity::getPath, path).or().eq(SysMenuEntity::getPath, routeName)
                ));
        for (SysMenuEntity sysMenu : sysMenuList) {
            if (!sysMenu.getMenuId().equals(menuId)) {
                Long dbParentId = sysMenu.getParentId();
                String dbPath = sysMenu.getPath();
                String dbRouteName = StringUtils.isEmpty(sysMenu.getRouteName()) ? dbPath : sysMenu.getRouteName();
                if (StringUtils.equalsAnyIgnoreCase(path, dbPath) && parentId.equals(dbParentId)) {
                    log.warn("[同级路由冲突] 同级下已存在相同路由路径 '{}'，冲突菜单：{}", dbPath, sysMenu.getMenuName());
                    return false;
                } else if (StringUtils.equalsAnyIgnoreCase(path, dbPath)
                    && Constants.TOP_PARENT_ID.equals(parentId)
                    && Constants.TOP_PARENT_ID.equals(dbParentId)) {
                    log.warn("[根目录路由冲突] 根目录下路由 '{}' 必须唯一，已被菜单 '{}' 占用", path, sysMenu.getMenuName());
                    return false;
                } else if (StringUtils.equalsAnyIgnoreCase(routeName, dbRouteName)
                    && sysMenu.getMenuType().equals(menu.getMenuType())) {
                    log.warn("[路由名称冲突] 路由名称 '{}' 需全局唯一，已被菜单 '{}' 使用", routeName, sysMenu.getMenuName());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    private List<SysMenuEntity> getChildPerms(List<SysMenuEntity> list, Long parentId) {
        List<SysMenuEntity> returnList = new ArrayList<>();
        for (SysMenuEntity t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId().equals(parentId)) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysMenuEntity> list, SysMenuEntity t) {
        // 得到子节点列表
        List<SysMenuEntity> childList = StreamUtils.filter(list, n -> n.getParentId().equals(t.getMenuId()));
        t.setChildren(childList);
        for (SysMenuEntity tChild : childList) {
            // 判断是否有子节点
            if (list.stream().anyMatch(n -> n.getParentId().equals(tChild.getMenuId()))) {
                recursionFn(list, tChild);
            }
        }
    }

}
