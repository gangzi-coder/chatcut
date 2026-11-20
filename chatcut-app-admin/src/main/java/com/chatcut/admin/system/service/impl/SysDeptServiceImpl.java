package com.chatcut.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.constant.CacheNames;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.dto.DeptDTO;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.service.DeptService;
import com.chatcut.common.core.utils.*;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.mybatis.helper.DataBaseHelper;
import com.chatcut.common.redis.utils.CacheUtils;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.admin.system.repository.entity.SysDeptEntity;
import com.chatcut.admin.system.repository.entity.SysRoleEntity;
import com.chatcut.admin.system.repository.entity.SysUserEntity;
import com.chatcut.admin.system.model.req.SysDeptReq;
import com.chatcut.admin.system.model.resp.SysDeptResp;
import com.chatcut.admin.system.repository.mapper.SysDeptMapper;
import com.chatcut.admin.system.repository.mapper.SysRoleMapper;
import com.chatcut.admin.system.repository.mapper.SysUserMapper;
import com.chatcut.admin.system.service.ISysDeptService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 部门管理 服务实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysDeptServiceImpl implements ISysDeptService, DeptService {

    private final SysDeptMapper baseMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserMapper userMapper;

    /**
     * 分页查询部门管理数据
     *
     * @param dept      部门信息
     * @param pageQuery 分页对象
     * @return 部门信息集合
     */
    @Override
    public TableDataInfo<SysDeptResp> selectPageDeptList(SysDeptReq dept, PageQuery pageQuery) {
        Page<SysDeptResp> page = baseMapper.selectPageDeptList(pageQuery.build(), buildQueryWrapper(dept));
        return TableDataInfo.build(page);
    }

    /**
     * 查询部门管理数据
     *
     * @param dept 部门信息
     * @return 部门信息集合
     */
    @Override
    public List<SysDeptResp> selectDeptList(SysDeptReq dept) {
        LambdaQueryWrapper<SysDeptEntity> lqw = buildQueryWrapper(dept);
        return baseMapper.selectDeptList(lqw);
    }

    /**
     * 查询部门树结构信息
     *
     * @param bo 部门信息
     * @return 部门树信息集合
     */
    @Override
    public List<Tree<Long>> selectDeptTreeList(SysDeptReq bo) {
        LambdaQueryWrapper<SysDeptEntity> lqw = buildQueryWrapper(bo);
        List<SysDeptResp> depts = baseMapper.selectDeptList(lqw);
        return buildDeptTreeSelect(depts);
    }

    private LambdaQueryWrapper<SysDeptEntity> buildQueryWrapper(SysDeptReq bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysDeptEntity> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysDeptEntity::getDelFlag, SystemConstants.NORMAL);
        lqw.eq(ObjectUtil.isNotNull(bo.getDeptId()), SysDeptEntity::getDeptId, bo.getDeptId());
        lqw.eq(ObjectUtil.isNotNull(bo.getParentId()), SysDeptEntity::getParentId, bo.getParentId());
        lqw.like(StringUtils.isNotBlank(bo.getDeptName()), SysDeptEntity::getDeptName, bo.getDeptName());
        lqw.like(StringUtils.isNotBlank(bo.getDeptCategory()), SysDeptEntity::getDeptCategory, bo.getDeptCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysDeptEntity::getStatus, bo.getStatus());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysDeptEntity::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(SysDeptEntity::getAncestors);
        lqw.orderByAsc(SysDeptEntity::getParentId);
        lqw.orderByAsc(SysDeptEntity::getOrderNum);
        lqw.orderByAsc(SysDeptEntity::getDeptId);
        if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            //部门树搜索
            lqw.and(x -> {
                List<Long> deptIds = baseMapper.selectDeptAndChildById(bo.getBelongDeptId());
                x.in(SysDeptEntity::getDeptId, deptIds);
            });
        }
        return lqw;
    }

    /**
     * 构建前端所需要下拉树结构
     *
     * @param depts 部门列表
     * @return 下拉树结构列表
     */
    @Override
    public List<Tree<Long>> buildDeptTreeSelect(List<SysDeptResp> depts) {
        if (CollUtil.isEmpty(depts)) {
            return CollUtil.newArrayList();
        }
        return TreeBuildUtils.buildMultiRoot(
            depts,
            SysDeptResp::getDeptId,
            SysDeptResp::getParentId,
            (node, treeNode) -> treeNode
                .setId(node.getDeptId())
                .setParentId(node.getParentId())
                .setName(node.getDeptName())
                .setWeight(node.getOrderNum())
                .putExtra("disabled", SystemConstants.DISABLE.equals(node.getStatus()))
        );
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId 角色ID
     * @return 选中部门列表
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId) {
        SysRoleEntity role = roleMapper.selectById(roleId);
        return baseMapper.selectDeptListByRoleId(roleId, role.getDeptCheckStrictly());
    }

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    @Cacheable(cacheNames = CacheNames.SYS_DEPT, key = "#deptId")
    @Override
    public SysDeptResp selectDeptById(Long deptId) {
        SysDeptResp dept = baseMapper.selectVoById(deptId);
        if (ObjectUtil.isNull(dept)) {
            return null;
        }
        SysDeptResp parentDept = baseMapper.selectVoOne(new LambdaQueryWrapper<SysDeptEntity>()
            .select(SysDeptEntity::getDeptName).eq(SysDeptEntity::getDeptId, dept.getParentId()));
        dept.setParentName(ObjectUtils.notNullGetter(parentDept, SysDeptResp::getDeptName));
        return dept;
    }

    @Override
    public List<SysDeptResp> selectDeptByIds(List<Long> deptIds) {
        return baseMapper.selectDeptList(new LambdaQueryWrapper<SysDeptEntity>()
            .select(SysDeptEntity::getDeptId, SysDeptEntity::getDeptName, SysDeptEntity::getLeader)
            .eq(SysDeptEntity::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(deptIds), SysDeptEntity::getDeptId, deptIds));
    }

    /**
     * 通过部门ID查询部门名称
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    @Override
    public String selectDeptNameByIds(String deptIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(deptIds, Convert::toLong)) {
            SysDeptResp vo = SpringUtils.getAopProxy(this).selectDeptById(id);
            if (ObjectUtil.isNotNull(vo)) {
                list.add(vo.getDeptName());
            }
        }
        return StringUtils.joinComma(list);
    }

    /**
     * 根据部门ID查询部门负责人
     *
     * @param deptId 部门ID，用于指定需要查询的部门
     * @return 返回该部门的负责人ID
     */
    @Override
    public Long selectDeptLeaderById(Long deptId) {
        SysDeptResp vo = SpringUtils.getAopProxy(this).selectDeptById(deptId);
        return vo.getLeader();
    }

    /**
     * 查询部门
     *
     * @return 部门列表
     */
    @Override
    public List<DeptDTO> selectDeptsByList() {
        List<SysDeptResp> list = baseMapper.selectDeptList(new LambdaQueryWrapper<SysDeptEntity>()
            .select(SysDeptEntity::getDeptId, SysDeptEntity::getDeptName, SysDeptEntity::getParentId)
            .eq(SysDeptEntity::getStatus, SystemConstants.NORMAL));
        return BeanUtil.copyToList(list, DeptDTO.class);
    }

    /**
     * 根据ID查询所有子部门数（正常状态）
     *
     * @param deptId 部门ID
     * @return 子部门数
     */
    @Override
    public long selectNormalChildrenDeptById(Long deptId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysDeptEntity>()
            .eq(SysDeptEntity::getStatus, SystemConstants.NORMAL)
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
    }

    /**
     * 是否存在子节点
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Override
    public boolean hasChildByDeptId(Long deptId) {
        return baseMapper.exists(new LambdaQueryWrapper<SysDeptEntity>()
            .eq(SysDeptEntity::getParentId, deptId));
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果 true 存在 false 不存在
     */
    @Override
    public boolean checkDeptExistUser(Long deptId) {
        return userMapper.exists(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getDeptId, deptId));
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param dept 部门信息
     * @return 结果
     */
    @Override
    public boolean checkDeptNameUnique(SysDeptReq dept) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysDeptEntity>()
            .eq(SysDeptEntity::getDeptName, dept.getDeptName())
            .eq(SysDeptEntity::getParentId, dept.getParentId())
            .ne(ObjectUtil.isNotNull(dept.getDeptId()), SysDeptEntity::getDeptId, dept.getDeptId()));
        return !exist;
    }

    /**
     * 校验部门是否有数据权限
     *
     * @param deptId 部门id
     */
    @Override
    public void checkDeptDataScope(Long deptId) {
        if (ObjectUtil.isNull(deptId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (baseMapper.countDeptById(deptId) == 0) {
            throw new ServiceException("没有权限访问部门数据！");
        }
    }

    /**
     * 新增保存部门信息
     *
     * @param bo 部门信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    @Override
    public int insertDept(SysDeptReq bo) {
        SysDeptEntity info = baseMapper.selectById(bo.getParentId());
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!SystemConstants.NORMAL.equals(info.getStatus())) {
            throw new ServiceException("部门停用，不允许新增");
        }
        SysDeptEntity dept = MapstructUtils.convert(bo, SysDeptEntity.class);
        dept.setAncestors(info.getAncestors() + StringUtils.SEPARATOR + dept.getParentId());
        return baseMapper.insert(dept);
    }

    /**
     * 修改保存部门信息
     *
     * @param bo 部门信息
     * @return 结果
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#bo.deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, allEntries = true)
    })
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDept(SysDeptReq bo) {
        SysDeptEntity dept = MapstructUtils.convert(bo, SysDeptEntity.class);
        SysDeptEntity oldDept = baseMapper.selectById(dept.getDeptId());
        if (ObjectUtil.isNull(oldDept)) {
            throw new ServiceException("部门不存在，无法修改");
        }
        if (!oldDept.getParentId().equals(dept.getParentId())) {
            // 如果是新父部门 则校验是否具有新父部门权限 避免越权
            this.checkDeptDataScope(dept.getParentId());
            SysDeptEntity newParentDept = baseMapper.selectById(dept.getParentId());
            if (ObjectUtil.isNotNull(newParentDept)) {
                String newAncestors = newParentDept.getAncestors() + StringUtils.SEPARATOR + newParentDept.getDeptId();
                String oldAncestors = oldDept.getAncestors();
                dept.setAncestors(newAncestors);
                updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
            }
        } else {
            dept.setAncestors(oldDept.getAncestors());
        }
        int result = baseMapper.updateById(dept);
        // 如果部门状态为启用，且部门祖级列表不为空，且部门祖级列表不等于根部门祖级列表（如果部门祖级列表不等于根部门祖级列表，则说明存在上级部门）
        if (SystemConstants.NORMAL.equals(dept.getStatus())
            && StringUtils.isNotEmpty(dept.getAncestors())
            && !StringUtils.equals(SystemConstants.ROOT_DEPT_ANCESTORS, dept.getAncestors())) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 修改该部门的父级部门状态
     *
     * @param dept 当前部门
     */
    private void updateParentDeptStatusNormal(SysDeptEntity dept) {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        baseMapper.update(null, new LambdaUpdateWrapper<SysDeptEntity>()
            .set(SysDeptEntity::getStatus, SystemConstants.NORMAL)
            .in(SysDeptEntity::getDeptId, Arrays.asList(deptIds)));
    }

    /**
     * 修改子元素关系
     *
     * @param deptId       被修改的部门ID
     * @param newAncestors 新的父ID集合
     * @param oldAncestors 旧的父ID集合
     */
    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDeptEntity> children = baseMapper.selectList(new LambdaQueryWrapper<SysDeptEntity>()
            .apply(DataBaseHelper.findInSet(deptId, "ancestors")));
        List<SysDeptEntity> list = new ArrayList<>();
        for (SysDeptEntity child : children) {
            SysDeptEntity dept = new SysDeptEntity();
            dept.setDeptId(child.getDeptId());
            dept.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
            list.add(dept);
        }
        if (CollUtil.isNotEmpty(list)) {
            if (baseMapper.updateBatchById(list)) {
                list.forEach(dept -> CacheUtils.evict(CacheNames.SYS_DEPT, dept.getDeptId()));
            }
        }
    }

    /**
     * 删除部门管理信息
     *
     * @param deptId 部门ID
     * @return 结果
     */
    @Caching(evict = {
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT, key = "#deptId"),
        @CacheEvict(cacheNames = CacheNames.SYS_DEPT_AND_CHILD, key = "#deptId")
    })
    @Override
    public int deleteDeptById(Long deptId) {
        return baseMapper.deleteById(deptId);
    }


    /**
     * 根据部门 ID 列表查询部门名称映射关系
     *
     * @param deptIds 部门 ID 列表
     * @return Map，其中 key 为部门 ID，value 为对应的部门名称
     */
    @Override
    public Map<Long, String> selectDeptNamesByIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyMap();
        }
        List<SysDeptEntity> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysDeptEntity>()
                .select(SysDeptEntity::getDeptId, SysDeptEntity::getDeptName)
                .in(SysDeptEntity::getDeptId, deptIds)
        );
        return StreamUtils.toMap(list, SysDeptEntity::getDeptId, SysDeptEntity::getDeptName);
    }

}
