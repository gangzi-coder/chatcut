package com.chatcut.admin.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.service.PostService;
import com.chatcut.common.core.utils.MapstructUtils;
import com.chatcut.common.core.utils.StreamUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.repository.entity.SysPostEntity;
import com.chatcut.admin.system.repository.entity.SysUserPostEntity;
import com.chatcut.admin.system.model.req.SysPostReq;
import com.chatcut.admin.system.model.resp.SysPostResp;
import com.chatcut.admin.system.repository.mapper.SysDeptMapper;
import com.chatcut.admin.system.repository.mapper.SysPostMapper;
import com.chatcut.admin.system.repository.mapper.SysUserPostMapper;
import com.chatcut.admin.system.service.ISysPostService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 岗位信息 服务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysPostServiceImpl implements ISysPostService, PostService {

    private final SysPostMapper baseMapper;
    private final SysDeptMapper deptMapper;
    private final SysUserPostMapper userPostMapper;

    /**
     * 分页查询岗位列表
     *
     * @param post      查询条件
     * @param pageQuery 分页参数
     * @return 岗位分页列表
     */
    @Override
    public TableDataInfo<SysPostResp> selectPagePostList(SysPostReq post, PageQuery pageQuery) {
        Page<SysPostResp> page = baseMapper.selectPagePostList(pageQuery.build(), buildQueryWrapper(post));
        return TableDataInfo.build(page);
    }

    /**
     * 查询岗位信息集合
     *
     * @param post 岗位信息
     * @return 岗位信息集合
     */
    @Override
    public List<SysPostResp> selectPostList(SysPostReq post) {
        return baseMapper.selectVoList(buildQueryWrapper(post));
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 岗位ID
     */
    @Override
    public List<SysPostResp> selectPostsByUserId(Long userId) {
        return baseMapper.selectPostsByUserId(userId);
    }

    /**
     * 根据查询条件构建查询包装器
     *
     * @param bo 查询条件对象
     * @return 构建好的查询包装器
     */
    private LambdaQueryWrapper<SysPostEntity> buildQueryWrapper(SysPostReq bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysPostEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(bo.getPostCode()), SysPostEntity::getPostCode, bo.getPostCode())
            .like(StringUtils.isNotBlank(bo.getPostCategory()), SysPostEntity::getPostCategory, bo.getPostCategory())
            .like(StringUtils.isNotBlank(bo.getPostName()), SysPostEntity::getPostName, bo.getPostName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), SysPostEntity::getStatus, bo.getStatus())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysPostEntity::getCreateTime, params.get("beginTime"), params.get("endTime"))
            .orderByAsc(SysPostEntity::getPostSort);
        if (ObjectUtil.isNotNull(bo.getDeptId())) {
            //优先单部门搜索
            wrapper.eq(SysPostEntity::getDeptId, bo.getDeptId());
        } else if (ObjectUtil.isNotNull(bo.getBelongDeptId())) {
            //部门树搜索
            wrapper.and(x -> {
                List<Long> deptIds = deptMapper.selectDeptAndChildById(bo.getBelongDeptId());
                x.in(SysPostEntity::getDeptId, deptIds);
            });
        }
        return wrapper;
    }

    /**
     * 查询所有岗位
     *
     * @return 岗位列表
     */
    @Override
    public List<SysPostResp> selectPostAll() {
        return baseMapper.selectVoList(new QueryWrapper<>());
    }

    /**
     * 通过岗位ID查询岗位信息
     *
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPostResp selectPostById(Long postId) {
        return baseMapper.selectVoById(postId);
    }

    /**
     * 根据用户ID获取岗位选择框列表
     *
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    @Override
    public List<Long> selectPostListByUserId(Long userId) {
        List<SysPostResp> list = baseMapper.selectPostsByUserId(userId);
        return StreamUtils.toList(list, SysPostResp::getPostId);
    }

    /**
     * 通过岗位ID串查询岗位
     *
     * @param postIds 岗位id串
     * @return 岗位列表信息
     */
    @Override
    public List<SysPostResp> selectPostByIds(List<Long> postIds) {
        return baseMapper.selectVoList(new LambdaQueryWrapper<SysPostEntity>()
            .select(SysPostEntity::getPostId, SysPostEntity::getPostName, SysPostEntity::getPostCode)
            .eq(SysPostEntity::getStatus, SystemConstants.NORMAL)
            .in(CollUtil.isNotEmpty(postIds), SysPostEntity::getPostId, postIds));
    }

    /**
     * 校验岗位名称是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPostReq post) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysPostEntity>()
            .eq(SysPostEntity::getPostName, post.getPostName())
            .eq(SysPostEntity::getDeptId, post.getDeptId())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPostEntity::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 校验岗位编码是否唯一
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPostReq post) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysPostEntity>()
            .eq(SysPostEntity::getPostCode, post.getPostCode())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPostEntity::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostById(Long postId) {
        return userPostMapper.selectCount(new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getPostId, postId));
    }

    /**
     * 通过部门ID查询岗位使用数量
     *
     * @param deptId 部门id
     * @return 结果
     */
    @Override
    public long countPostByDeptId(Long deptId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<SysPostEntity>().eq(SysPostEntity::getDeptId, deptId));
    }

    /**
     * 删除岗位信息
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public int deletePostById(Long postId) {
        return baseMapper.deleteById(postId);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(List<Long> postIds) {
        List<SysPostEntity> list = baseMapper.selectByIds(postIds);
        for (SysPostEntity post : list) {
            if (this.countUserPostById(post.getPostId()) > 0) {
                throw new ServiceException("{}已分配，不能删除!", post.getPostName());
            }
        }
        return baseMapper.deleteByIds(postIds);
    }

    /**
     * 新增保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPostReq bo) {
        SysPostEntity post = MapstructUtils.convert(bo, SysPostEntity.class);
        return baseMapper.insert(post);
    }

    /**
     * 修改保存岗位信息
     *
     * @param bo 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPostReq bo) {
        SysPostEntity post = MapstructUtils.convert(bo, SysPostEntity.class);
        return baseMapper.updateById(post);
    }

    /**
     * 根据岗位 ID 列表查询岗位名称映射关系
     *
     * @param postIds 岗位 ID 列表
     * @return Map，其中 key 为岗位 ID，value 为对应的岗位名称
     */
    @Override
    public Map<Long, String> selectPostNamesByIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return Collections.emptyMap();
        }
        List<SysPostEntity> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysPostEntity>()
                .select(SysPostEntity::getPostId, SysPostEntity::getPostName)
                .in(SysPostEntity::getPostId, postIds)
        );
        return StreamUtils.toMap(list, SysPostEntity::getPostId, SysPostEntity::getPostName);
    }

}
