package com.chatcut.admin.system.repository.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chatcut.common.mybatis.annotation.DataColumn;
import com.chatcut.common.mybatis.annotation.DataPermission;
import com.chatcut.common.mybatis.core.mapper.BaseMapperPlus;
import com.chatcut.admin.system.repository.entity.SysPostEntity;
import com.chatcut.admin.system.model.resp.SysPostResp;

import java.util.List;

/**
 * 岗位信息 数据层
 *
 * @author Lion Li
 */
public interface SysPostMapper extends BaseMapperPlus<SysPostEntity, SysPostResp> {

    /**
     * 分页查询岗位列表
     *
     * @param page         分页对象
     * @param queryWrapper 查询条件
     * @return 包含岗位信息的分页结果
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default Page<SysPostResp> selectPagePostList(Page<SysPostEntity> page, Wrapper<SysPostEntity> queryWrapper) {
        return this.selectVoPage(page, queryWrapper);
    }

    /**
     * 查询岗位列表
     *
     * @param queryWrapper 查询条件
     * @return 岗位信息列表
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default List<SysPostResp> selectPostList(Wrapper<SysPostEntity> queryWrapper) {
        return this.selectVoList(queryWrapper);
    }

    /**
     * 根据岗位ID集合查询岗位数量
     *
     * @param postIds 岗位ID列表
     * @return 匹配的岗位数量
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id"),
        @DataColumn(key = "userName", value = "create_by")
    })
    default long selectPostCount(List<Long> postIds) {
        return this.selectCount(new LambdaQueryWrapper<SysPostEntity>().in(SysPostEntity::getPostId, postIds));
    }

    /**
     * 根据用户ID查询其关联的岗位列表
     *
     * @param userId 用户ID
     * @return 岗位信息列表
     */
    default List<SysPostResp> selectPostsByUserId(Long userId) {
        return this.selectVoList(new LambdaQueryWrapper<SysPostEntity>()
            .inSql(SysPostEntity::getPostId, "select post_id from sys_user_post where user_id = " + userId));
    }

}
