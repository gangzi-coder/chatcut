package com.chatcut.admin.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.idempotent.annotation.RepeatSubmit;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.web.core.BaseController;
import com.chatcut.admin.system.model.req.SysDeptReq;
import com.chatcut.admin.system.model.resp.SysDeptResp;
import com.chatcut.admin.system.service.ISysDeptService;
import com.chatcut.admin.system.service.ISysPostService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {

    private final ISysDeptService deptService;
    private final ISysPostService postService;

    /**
     * 获取部门列表
     */
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    public R<List<SysDeptResp>> list(SysDeptReq dept) {
        List<SysDeptResp> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }

    /**
     * 查询部门列表（排除节点）
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:list")
    @GetMapping("/listExcludeChild")
    public R<List<SysDeptResp>> excludeChild(@RequestParam(value = "deptId", required = false) String deptId) {
        Long id = StringUtils.isBlank(deptId) ? null : Long.valueOf(deptId);
        List<SysDeptResp> depts = deptService.selectDeptList(new SysDeptReq());
        depts.removeIf(d -> id != null && (d.getDeptId().equals(id)
            || StringUtils.splitList(d.getAncestors()).contains(Convert.toStr(id))));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping("/getSysDeptInfo")
    public R<SysDeptResp> getInfo(@RequestParam("id") String deptId) {
        Long id = Long.valueOf(deptId);
        deptService.checkDeptDataScope(id);
        return R.ok(deptService.selectDeptById(id));
    }

    /**
     * 新增部门
     */
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addSysDept")
    public R<Void> add(@Validated @RequestBody SysDeptReq dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/updateSysDept")
    public R<Void> edit(@Validated @RequestBody SysDeptReq dept) {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(deptId)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(SystemConstants.DISABLE, dept.getStatus())) {
            if (deptService.selectNormalChildrenDeptById(deptId) > 0) {
                return R.fail("该部门包含未停用的子部门!");
            } else if (deptService.checkDeptExistUser(deptId)) {
                return R.fail("该部门下存在已分配用户，不能禁用!");
            }
        }
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @PostMapping("/removeSysDept")
    public R<Void> remove(@RequestParam("id") String deptId) {
        Long id = Long.valueOf(deptId);
        if (SystemConstants.DEFAULT_DEPT_ID.equals(id)) {
            return R.warn("默认部门,不允许删除");
        }
        if (deptService.hasChildByDeptId(id)) {
            return R.warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(id)) {
            return R.warn("部门存在用户,不允许删除");
        }
        if (postService.countPostByDeptId(id) > 0) {
            return R.warn("部门存在岗位,不允许删除");
        }
        deptService.checkDeptDataScope(id);
        return toAjax(deptService.deleteDeptById(id));
    }

    /**
     * 获取部门选择框列表
     *
     * @param deptIds 部门ID串
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping("/optionselect")
    public R<List<SysDeptResp>> optionselect(@RequestParam(required = false) Long[] deptIds) {
        return R.ok(deptService.selectDeptByIds(deptIds == null ? null : List.of(deptIds)));
    }

}
