package com.chatcut.admin.system.service.impl;

import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.dto.TaskAssigneeDTO;
import com.chatcut.common.core.domain.model.TaskAssigneeBody;
import com.chatcut.common.core.service.TaskAssigneeService;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.model.req.SysDeptReq;
import com.chatcut.admin.system.model.req.SysPostReq;
import com.chatcut.admin.system.model.req.SysRoleReq;
import com.chatcut.admin.system.model.req.SysUserReq;
import com.chatcut.admin.system.model.resp.SysDeptResp;
import com.chatcut.admin.system.model.resp.SysPostResp;
import com.chatcut.admin.system.model.resp.SysRoleResp;
import com.chatcut.admin.system.model.resp.SysUserResp;
import com.chatcut.admin.system.service.ISysDeptService;
import com.chatcut.admin.system.service.ISysPostService;
import com.chatcut.admin.system.service.ISysRoleService;
import com.chatcut.admin.system.service.ISysUserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 工作流设计器获取任务执行人
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SysTaskAssigneeServiceImpl implements TaskAssigneeService {

    // 上级Service注入下级Service 其他Service永远不可能注入当前类 避免循环注入
    private final ISysPostService postService;
    private final ISysDeptService deptService;
    private final ISysUserService userService;
    private final ISysRoleService roleService;

    /**
     * 查询角色并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectRolesByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysRoleReq bo = new SysRoleReq();
        bo.setRoleKey(taskQuery.getHandlerCode());
        bo.setRoleName(taskQuery.getHandlerName());
        bo.setStatus(SystemConstants.NORMAL);
        Map<String, Object> params = bo.getParams();
        params.put("beginTime", taskQuery.getBeginTime());
        params.put("endTime", taskQuery.getEndTime());
        TableDataInfo<SysRoleResp> page = roleService.selectPageRoleList(bo, pageQuery);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRows(),
            item -> Convert.toStr(item.getRoleId()), SysRoleResp::getRoleKey, SysRoleResp::getRoleName, item -> "", SysRoleResp::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 查询岗位并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectPostsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysPostReq bo = new SysPostReq();
        bo.setPostCategory(taskQuery.getHandlerCode());
        bo.setPostName(taskQuery.getHandlerName());
        bo.setStatus(SystemConstants.NORMAL);
        Map<String, Object> params = bo.getParams();
        params.put("beginTime", taskQuery.getBeginTime());
        params.put("endTime", taskQuery.getEndTime());
        bo.setBelongDeptId(Convert.toLong(taskQuery.getGroupId()));
        TableDataInfo<SysPostResp> page = postService.selectPagePostList(bo, pageQuery);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRows(),
            item -> Convert.toStr(item.getPostId()), SysPostResp::getPostCategory, SysPostResp::getPostName, item -> Convert.toStr(item.getDeptId()), SysPostResp::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 查询部门并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectDeptsByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysDeptReq bo = new SysDeptReq();
        bo.setDeptCategory(taskQuery.getHandlerCode());
        bo.setDeptName(taskQuery.getHandlerName());
        bo.setStatus(SystemConstants.NORMAL);
        Map<String, Object> params = bo.getParams();
        params.put("beginTime", taskQuery.getBeginTime());
        params.put("endTime", taskQuery.getEndTime());
        bo.setBelongDeptId(Convert.toLong(taskQuery.getGroupId()));
        TableDataInfo<SysDeptResp> page = deptService.selectPageDeptList(bo, pageQuery);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRows(),
            item -> Convert.toStr(item.getDeptId()), SysDeptResp::getDeptCategory, SysDeptResp::getDeptName, item -> Convert.toStr(item.getParentId()), SysDeptResp::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

    /**
     * 查询用户并返回任务指派的列表，支持分页
     *
     * @param taskQuery 查询条件
     * @return 办理人
     */
    @Override
    public TaskAssigneeDTO selectUsersByTaskAssigneeList(TaskAssigneeBody taskQuery) {
        PageQuery pageQuery = new PageQuery(taskQuery.getPageSize(), taskQuery.getPageNum());
        SysUserReq bo = new SysUserReq();
        bo.setUserName(taskQuery.getHandlerCode());
        bo.setNickName(taskQuery.getHandlerName());
        bo.setStatus(SystemConstants.NORMAL);
        Map<String, Object> params = bo.getParams();
        params.put("beginTime", taskQuery.getBeginTime());
        params.put("endTime", taskQuery.getEndTime());
        bo.setDeptId(Convert.toLong(taskQuery.getGroupId()));
        TableDataInfo<SysUserResp> page = userService.selectPageUserList(bo, pageQuery);
        // 使用封装的字段映射方法进行转换
        List<TaskAssigneeDTO.TaskHandler> handlers = TaskAssigneeDTO.convertToHandlerList(page.getRows(),
            item -> Convert.toStr(item.getUserId()), SysUserResp::getUserName, SysUserResp::getNickName, item -> Convert.toStr(item.getDeptId()), SysUserResp::getCreateTime);
        return new TaskAssigneeDTO(page.getTotal(), handlers);
    }

}
