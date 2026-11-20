package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.common.core.utils.StreamUtils;
import com.chatcut.common.mybatis.annotation.DataColumn;
import com.chatcut.common.mybatis.annotation.DataPermission;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.common.mybatis.helper.DataBaseHelper;
import com.chatcut.admin.system.repository.entity.SysDeptEntity;
import com.chatcut.admin.system.model.resp.SysDeptResp;

import java.util.List;

/**
 * 部门管理 数据层
 *
 * @author Lion Li
 */
public interface SysDeptMapper extends BaseMapperPlus<SysDeptEntity, SysDeptResp> {

    /**
     * 构建角色对应的部门 SQL 查询语句
     *
     * <p>该 SQL 用于查询某个角色关联的所有部门 ID，常用于数据权限控制</p>
     *
     * @param roleId 角色ID
     * @return 查询部门ID的 SQL 语句字符串
     */
    default String buildDeptByRoleSql(Long roleId) {
        return """
                select srd.dept_id from sys_role_dept srd
                    left join sys_role sr on sr.role_id = srd.role_id
                    where srd.role_id = %d and sr.status = '0'
            """.formatted(roleId);
    }

    /**
     * 构建 SQL 查询，用于获取当前角色拥有的部门中所有的父部门ID
     *
     * <p>
     * 该 SQL 用于 deptCheckStrictly 场景下，排除非叶子节点（父节点）用。
     * </p>
     *
     * @param roleId 角色ID
     * @return SQL 语句字符串，查询角色下部门的所有父部门ID
     */
    default String buildParentDeptByRoleSql(Long roleId) {
        return """
                select parent_id from sys_dept where dept_id in (
                    select srd.dept_id from sys_role_dept srd
                        left join sys_role sr on sr.role_id = srd.role_id
                        where srd.role_id = %d and sr.status = '0'
                )
            """.formatted(roleId);
    }

    /**
     * 查询部门管理数据
     *
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    default List<SysDeptResp> selectDeptList(Wrapper<SysDeptEntity> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

    /**
     * 分页查询部门管理数据
     *
     * @param page         分页信息
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
    })
    default Page<SysDeptResp> selectPageDeptList(Page<SysDeptEntity> page, Wrapper<SysDeptEntity> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 统计指定部门ID的部门数量
     *
     * @param deptId 部门ID
     * @return 该部门ID的部门数量
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    default long countDeptById(Long deptId) {
        return this.selectCount(new LambdaQueryWrapper<SysDeptEntity>().eq(SysDeptEntity::getDeptId, deptId));
    }

    /**
     * 根据父部门ID查询其所有子部门的列表
     *
     * @param parentId 父部门ID
     * @return 包含子部门的列表
     */
    default List<SysDeptEntity> selectListByParentId(Long parentId) {
        return this.selectList(new LambdaQueryWrapper<SysDeptEntity>()
            .select(SysDeptEntity::getDeptId)
            .apply(DataBaseHelper.findInSet(parentId, "ancestors")));
    }

    /**
     * 查询某个部门及其所有子部门ID（含自身）
     *
     * @param parentId 父部门ID
     * @return 部门ID集合
     */
    default List<Long> selectDeptAndChildById(Long parentId) {
        List<SysDeptEntity> deptList = this.selectListByParentId(parentId);
        List<Long> deptIds = StreamUtils.toList(deptList, SysDeptEntity::getDeptId);
        deptIds.add(parentId);
        return deptIds;
    }

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    default List<Long> selectDeptListByRoleId(Long roleId, boolean deptCheckStrictly) {
        LambdaQueryWrapper<SysDeptEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(SysDeptEntity::getDeptId)
            .inSql(SysDeptEntity::getDeptId, this.buildDeptByRoleSql(roleId))
            .orderByAsc(SysDeptEntity::getParentId)
            .orderByAsc(SysDeptEntity::getOrderNum);
        if (deptCheckStrictly) {
            wrapper.notInSql(SysDeptEntity::getDeptId, this.buildParentDeptByRoleSql(roleId));
        }
        return this.selectObjs(wrapper);
    }

}
