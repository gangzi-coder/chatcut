package com.chatcut.admin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.CacheNames;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.dto.UserDTO;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.service.UserService;
import com.chatcut.common.core.utils.*;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.admin.system.repository.entity.SysUserEntity;
import com.chatcut.admin.system.repository.entity.SysUserPostEntity;
import com.chatcut.admin.system.repository.entity.SysUserRoleEntity;
import com.chatcut.admin.system.model.req.SysUserReq;
import com.chatcut.admin.system.model.resp.SysPostResp;
import com.chatcut.admin.system.model.resp.SysRoleResp;
import com.chatcut.admin.system.model.resp.SysUserExportResp;
import com.chatcut.admin.system.model.resp.SysUserResp;
import com.chatcut.admin.system.repository.mapper.*;
import com.chatcut.admin.system.service.ISysUserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService, UserService {

    private final SysUserMapper baseMapper;
    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysUserPostMapper userPostMapper;

    @Override
    public TableDataInfo<SysUserResp> selectPageUserList(SysUserReq user, PageQuery pageQuery) {
        Page<SysUserResp> page = baseMapper.selectPageUserList(pageQuery.build(), this.buildQueryWrapper(user));
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询用户列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserExportResp> selectUserExportList(SysUserReq user) {
        Map<String, Object> params = user.getParams();
        QueryWrapper<SysUserEntity> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getNickName()), "u.nick_name", user.getNickName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "u.create_time", params.get("beginTime"), params.get("endTime"))
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                List<Long> deptIds = deptMapper.selectDeptAndChildById(user.getDeptId());
                w.in("u.dept_id", deptIds);
            }).orderByAsc("u.user_id");
        return baseMapper.selectUserExportList(wrapper);
    }

    private Wrapper<SysUserEntity> buildQueryWrapper(SysUserReq user) {
        Map<String, Object> params = user.getParams();
        LambdaQueryWrapper<SysUserEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUserEntity::getDelFlag, SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(user.getUserId()), SysUserEntity::getUserId, user.getUserId())
            .in(StringUtils.isNotBlank(user.getUserIds()), SysUserEntity::getUserId, StringUtils.splitTo(user.getUserIds(), Convert::toLong))
            .like(StringUtils.isNotBlank(user.getUserName()), SysUserEntity::getUserName, user.getUserName())
            .like(StringUtils.isNotBlank(user.getNickName()), SysUserEntity::getNickName, user.getNickName())
            .eq(StringUtils.isNotBlank(user.getStatus()), SysUserEntity::getStatus, user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), SysUserEntity::getPhonenumber, user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysUserEntity::getCreateTime, params.get("beginTime"), params.get("endTime"))
            .and(ObjectUtil.isNotNull(user.getDeptId()), w -> {
                List<Long> ids = deptMapper.selectDeptAndChildById(user.getDeptId());
                w.in(SysUserEntity::getDeptId, ids);
            }).orderByAsc(SysUserEntity::getUserId);
        if (StringUtils.isNotBlank(user.getExcludeUserIds())) {
            wrapper.notIn(SysUserEntity::getUserId, StringUtils.splitTo(user.getExcludeUserIds(), Convert::toLong));
        }
        return wrapper;
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUserResp> selectAllocatedList(SysUserReq user, PageQuery pageQuery) {
        QueryWrapper<SysUserEntity> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        Page<SysUserResp> page = baseMapper.selectAllocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户信息集合信息
     */
    @Override
    public TableDataInfo<SysUserResp> selectUnallocatedList(SysUserReq user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper<SysUserEntity> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", SystemConstants.NORMAL)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .orderByAsc("u.user_id");
        Page<SysUserResp> page = baseMapper.selectUnallocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUserResp selectUserByUserName(String userName) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getUserName, userName));
    }

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    @Override
    public SysUserResp selectUserByPhonenumber(String phonenumber) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getPhonenumber, phonenumber));
    }

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    @Override
    public SysUserResp selectUserById(Long userId) {
        SysUserResp user = baseMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            return user;
        }
        user.setRoles(roleMapper.selectRolesByUserId(user.getUserId()));
        return user;
    }

    /**
     * 通过用户ID串查询用户
     *
     * @param userIds 用户ID串
     * @param deptId  部门id
     * @return 用户列表信息
     */
    @Override
    public List<SysUserResp> selectUserByIds(List<Long> userIds, Long deptId) {
        return baseMapper.selectUserList(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getUserId, SysUserEntity::getUserName, SysUserEntity::getNickName)
            .eq(SysUserEntity::getStatus, SystemConstants.NORMAL)
            .eq(ObjectUtil.isNotNull(deptId), SysUserEntity::getDeptId, deptId)
            .in(CollUtil.isNotEmpty(userIds), SysUserEntity::getUserId, userIds));
    }

    /**
     * 查询用户所属角色组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(Long userId) {
        List<SysRoleResp> list = roleMapper.selectRolesByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRoleResp::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(Long userId) {
        List<SysPostResp> list = postMapper.selectPostsByUserId(userId);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPostResp::getPostName);
    }

    /**
     * 校验用户账号是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUserReq user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUserEntity::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验手机号码是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkPhoneUnique(SysUserReq user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUserEntity::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验email是否唯一
     *
     * @param user 用户信息
     */
    @Override
    public boolean checkEmailUnique(SysUserReq user) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUserEntity::getUserId, user.getUserId()));
        return !exist;
    }

    /**
     * 校验用户是否允许操作
     *
     * @param userId 用户ID
     */
    @Override
    public void checkUserAllowed(Long userId) {
        if (ObjectUtil.isNotNull(userId) && LoginHelper.isSuperAdmin(userId)) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
    }

    /**
     * 校验用户是否有数据权限
     *
     * @param userId 用户id
     */
    @Override
    public void checkUserDataScope(Long userId) {
        if (ObjectUtil.isNull(userId)) {
            return;
        }
        if (LoginHelper.isSuperAdmin()) {
            return;
        }
        if (baseMapper.countUserById(userId) == 0) {
            throw new ServiceException("没有权限访问用户数据！");
        }
    }

    /**
     * 新增保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertUser(SysUserReq user) {
        SysUserEntity sysUser = MapstructUtils.convert(user, SysUserEntity.class);
        // 新增用户信息
        int rows = baseMapper.insert(sysUser);
        user.setUserId(sysUser.getUserId());
        // 新增用户岗位关联
        insertUserPost(user, false);
        // 新增用户与角色管理
        insertUserRole(user, false);
        return rows;
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUserReq user, String tenantId) {
        user.setCreateBy(0L);
        user.setUpdateBy(0L);
        SysUserEntity sysUser = MapstructUtils.convert(user, SysUserEntity.class);
        return baseMapper.insert(sysUser) > 0;
    }

    /**
     * 修改保存用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(SysUserReq user) {
        // 新增用户与角色管理
        insertUserRole(user, true);
        // 新增用户与岗位管理
        insertUserPost(user, true);
        SysUserEntity sysUser = MapstructUtils.convert(user, SysUserEntity.class);
        // 防止错误更新后导致的数据误删除
        int flag = baseMapper.updateById(sysUser);
        if (flag < 1) {
            throw new ServiceException("修改用户{}信息失败", user.getUserName());
        }
        return flag;
    }

    /**
     * 用户授权角色
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserAuth(Long userId, Long[] roleIds) {
        insertUserRole(userId, roleIds, true);
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 账号状态
     * @return 结果
     */
    @Override
    public int updateUserStatus(Long userId, String status) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUserEntity>()
                .set(SysUserEntity::getStatus, status)
                .eq(SysUserEntity::getUserId, userId));
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @CacheEvict(cacheNames = CacheNames.SYS_NICKNAME, key = "#user.userId")
    @Override
    public int updateUserProfile(SysUserReq user) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUserEntity>()
                .set(ObjectUtil.isNotNull(user.getNickName()), SysUserEntity::getNickName, user.getNickName())
                .set(SysUserEntity::getPhonenumber, user.getPhonenumber())
                .set(SysUserEntity::getEmail, user.getEmail())
                .set(SysUserEntity::getSex, user.getSex())
                .eq(SysUserEntity::getUserId, user.getUserId()));
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像地址
     * @return 结果
     */
    @Override
    public boolean updateUserAvatar(Long userId, Long avatar) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUserEntity>()
                .set(SysUserEntity::getAvatar, avatar)
                .eq(SysUserEntity::getUserId, userId)) > 0;
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(Long userId, String password) {
        return baseMapper.update(null,
            new LambdaUpdateWrapper<SysUserEntity>()
                .set(SysUserEntity::getPassword, password)
                .eq(SysUserEntity::getUserId, userId));
    }

    /**
     * 新增用户角色信息
     *
     * @param user  用户对象
     * @param clear 清除已存在的关联数据
     */
    private void insertUserRole(SysUserReq user, boolean clear) {
        this.insertUserRole(user.getUserId(), user.getRoleIds(), clear);
    }

    /**
     * 新增用户岗位信息
     *
     * @param user  用户对象
     * @param clear 清除已存在的关联数据
     */
    private void insertUserPost(SysUserReq user, boolean clear) {
        Long[] postIdArr = user.getPostIds();
        if (ArrayUtil.isEmpty(postIdArr)) {
            return;
        }
        List<Long> postIds = Arrays.asList(postIdArr);

        // 校验是否有权限操作这些岗位（含数据权限控制）
        if (postMapper.selectPostCount(postIds) != postIds.size()) {
            throw new ServiceException("没有权限访问岗位的数据");
        }

        // 是否清除旧的用户岗位绑定
        if (clear) {
            userPostMapper.delete(new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getUserId, user.getUserId()));
        }

        // 构建用户岗位关联列表并批量插入
        List<SysUserPostEntity> list = StreamUtils.toList(postIds,
            postId -> {
                SysUserPostEntity up = new SysUserPostEntity();
                up.setUserId(user.getUserId());
                up.setPostId(postId);
                return up;
            });
        userPostMapper.insertBatch(list);
    }

    /**
     * 新增用户角色信息
     *
     * @param userId  用户ID
     * @param roleIds 角色组
     * @param clear   清除已存在的关联数据
     */
    private void insertUserRole(Long userId, Long[] roleIds, boolean clear) {
        if (ArrayUtil.isEmpty(roleIds)) {
            return;
        }

        List<Long> roleList = new ArrayList<>(Arrays.asList(roleIds));

        // 非超级管理员，禁止包含超级管理员角色
        if (!LoginHelper.isSuperAdmin(userId)) {
            roleList.remove(SystemConstants.SUPER_ADMIN_ID);
        }

        // 移除超管角色后若无剩余角色，说明仅选了超管角色且不允许分配，显式报错
        if (roleList.isEmpty()) {
            throw new ServiceException("不允许为普通用户分配超级管理员角色，请至少选择一个其他角色");
        }

        // 校验是否有权限访问这些角色（含数据权限控制）
        if (roleMapper.selectRoleCount(roleList) != roleList.size()) {
            throw new ServiceException("没有权限访问角色的数据");
        }

        // 是否清除原有绑定
        if (clear) {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        }

        // 批量插入用户-角色关联
        List<SysUserRoleEntity> list = StreamUtils.toList(roleList,
            roleId -> {
                SysUserRoleEntity ur = new SysUserRoleEntity();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            });
        userRoleMapper.insertBatch(list);
    }

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserById(Long userId) {
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getUserId, userId));
        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteById(userId);
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUserByIds(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(userId);
            checkUserDataScope(userId);
        }
        List<Long> ids = List.of(userIds);
        // 删除用户与角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().in(SysUserRoleEntity::getUserId, ids));
        // 删除用户与岗位表
        userPostMapper.delete(new LambdaQueryWrapper<SysUserPostEntity>().in(SysUserPostEntity::getUserId, ids));
        // 防止更新失败导致的数据删除
        int flag = baseMapper.deleteByIds(ids);
        if (flag < 1) {
            throw new ServiceException("删除用户失败!");
        }
        return flag;
    }

    /**
     * 通过部门id查询当前部门所有用户
     *
     * @param deptId 部门ID
     * @return 用户信息集合信息
     */
    @Override
    public List<SysUserResp> selectUserListByDept(Long deptId) {
        LambdaQueryWrapper<SysUserEntity> lqw = Wrappers.lambdaQuery();
        lqw.eq(SysUserEntity::getDeptId, deptId);
        lqw.orderByAsc(SysUserEntity::getUserId);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 通过用户ID查询用户账户
     *
     * @param userId 用户ID
     * @return 用户账户
     */
    @Cacheable(cacheNames = CacheNames.SYS_USER_NAME, key = "#userId")
    @Override
    public String selectUserNameById(Long userId) {
        SysUserEntity sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getUserName).eq(SysUserEntity::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUserEntity::getUserName);
    }

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    @Override
    @Cacheable(cacheNames = CacheNames.SYS_NICKNAME, key = "#userId")
    public String selectNicknameById(Long userId) {
        SysUserEntity sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getNickName).eq(SysUserEntity::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUserEntity::getNickName);
    }

    /**
     * 通过用户ID查询用户昵称
     *
     * @param userIds 用户ID 多个用逗号隔开
     * @return 用户昵称
     */
    @Override
    public String selectNicknameByIds(String userIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(userIds, Convert::toLong)) {
            String nickname = SpringUtils.getAopProxy(this).selectNicknameById(id);
            if (StringUtils.isNotBlank(nickname)) {
                list.add(nickname);
            }
        }
        return StringUtils.joinComma(list);
    }

    /**
     * 通过用户ID查询用户手机号
     *
     * @param userId 用户id
     * @return 用户手机号
     */
    @Override
    public String selectPhonenumberById(Long userId) {
        SysUserEntity sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getPhonenumber).eq(SysUserEntity::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUserEntity::getPhonenumber);
    }

    /**
     * 通过用户ID查询用户邮箱
     *
     * @param userId 用户id
     * @return 用户邮箱
     */
    @Override
    public String selectEmailById(Long userId) {
        SysUserEntity sysUser = baseMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getEmail).eq(SysUserEntity::getUserId, userId));
        return ObjectUtils.notNullGetter(sysUser, SysUserEntity::getEmail);
    }

    /**
     * 通过用户ID查询用户列表
     *
     * @param userIds 用户ids
     * @return 用户列表
     */
    @Override
    public List<UserDTO> selectListByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return List.of();
        }
        List<SysUserResp> list = baseMapper.selectVoList(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getUserId, SysUserEntity::getDeptId, SysUserEntity::getUserName,
                SysUserEntity::getNickName, SysUserEntity::getUserType, SysUserEntity::getEmail,
                SysUserEntity::getPhonenumber, SysUserEntity::getSex, SysUserEntity::getStatus,
                SysUserEntity::getCreateTime)
            .eq(SysUserEntity::getStatus, SystemConstants.NORMAL)
            .in(SysUserEntity::getUserId, userIds));
        return BeanUtil.copyToList(list, UserDTO.class);
    }

    /**
     * 通过角色ID查询用户ID
     *
     * @param roleIds 角色ids
     * @return 用户ids
     */
    @Override
    public List<Long> selectUserIdsByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }
        List<SysUserRoleEntity> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRoleEntity>().in(SysUserRoleEntity::getRoleId, roleIds));
        return StreamUtils.toList(userRoles, SysUserRoleEntity::getUserId);
    }

    /**
     * 通过角色ID查询用户
     *
     * @param roleIds 角色ids
     * @return 用户
     */
    @Override
    public List<UserDTO> selectUsersByRoleIds(List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return List.of();
        }

        // 通过角色ID获取用户角色信息
        List<SysUserRoleEntity> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRoleEntity>().in(SysUserRoleEntity::getRoleId, roleIds));

        // 获取用户ID列表
        Set<Long> userIds = StreamUtils.toSet(userRoles, SysUserRoleEntity::getUserId);

        return this.selectListByIds(new ArrayList<>(userIds));
    }

    /**
     * 通过部门ID查询用户
     *
     * @param deptIds 部门ids
     * @return 用户
     */
    @Override
    public List<UserDTO> selectUsersByDeptIds(List<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return List.of();
        }
        List<SysUserResp> list = baseMapper.selectVoList(new LambdaQueryWrapper<SysUserEntity>()
            .select(SysUserEntity::getUserId, SysUserEntity::getUserName, SysUserEntity::getNickName, SysUserEntity::getEmail, SysUserEntity::getPhonenumber)
            .eq(SysUserEntity::getStatus, SystemConstants.NORMAL)
            .in(SysUserEntity::getDeptId, deptIds));
        return BeanUtil.copyToList(list, UserDTO.class);
    }

    /**
     * 通过岗位ID查询用户
     *
     * @param postIds 岗位ids
     * @return 用户
     */
    @Override
    public List<UserDTO> selectUsersByPostIds(List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return List.of();
        }

        // 通过岗位ID获取用户岗位信息
        List<SysUserPostEntity> userPosts = userPostMapper.selectList(
            new LambdaQueryWrapper<SysUserPostEntity>().in(SysUserPostEntity::getPostId, postIds));

        // 获取用户ID列表
        Set<Long> userIds = StreamUtils.toSet(userPosts, SysUserPostEntity::getUserId);

        return this.selectListByIds(new ArrayList<>(userIds));
    }

    /**
     * 根据用户 ID 列表查询用户昵称映射关系
     *
     * @param userIds 用户 ID 列表
     * @return Map，其中 key 为用户 ID，value 为对应的用户昵称
     */
    @Override
    public Map<Long, String> selectUserNicksByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<SysUserEntity> list = baseMapper.selectList(
            new LambdaQueryWrapper<SysUserEntity>()
                .select(SysUserEntity::getUserId, SysUserEntity::getNickName)
                .in(SysUserEntity::getUserId, userIds)
        );
        return StreamUtils.toMap(list, SysUserEntity::getUserId, SysUserEntity::getNickName);
    }

}
