package com.chatcut.admin.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.validate.AddGroup;
import com.chatcut.common.core.validate.EditGroup;
import com.chatcut.common.core.validate.QueryGroup;
import com.chatcut.common.web.core.BaseController;
import com.chatcut.common.idempotent.annotation.RepeatSubmit;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.admin.system.model.req.SysOssConfigReq;
import com.chatcut.admin.system.model.resp.SysOssConfigResp;
import com.chatcut.admin.system.service.ISysOssConfigService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对象存储配置
 *
 * @author Lion Li
 * @author 孤舟烟雨
 * @date 2021-08-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/oss/config")
public class SysOssConfigController extends BaseController {

    private final ISysOssConfigService ossConfigService;

    /**
     * 查询对象存储配置列表
     */
    @SaCheckPermission("system:ossConfig:list")
    @GetMapping("/list")
    public TableDataInfo<SysOssConfigResp> list(@Validated(QueryGroup.class) SysOssConfigReq bo, PageQuery pageQuery) {
        return ossConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取对象存储配置详细信息
     *
     * @param ossConfigId OSS配置ID
     */
    @SaCheckPermission("system:ossConfig:list")
    @GetMapping("/getSysOssConfigInfo")
    public R<SysOssConfigResp> getInfo(@NotNull(message = "主键不能为空")
                                     @RequestParam("id") String ossConfigId) {
        return R.ok(ossConfigService.queryById(Long.valueOf(ossConfigId)));
    }

    /**
     * 新增对象存储配置
     */
    @SaCheckPermission("system:ossConfig:add")
    @Log(title = "对象存储配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/addSysOssConfig")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SysOssConfigReq bo) {
        return toAjax(ossConfigService.insertByBo(bo));
    }

    /**
     * 修改对象存储配置
     */
    @SaCheckPermission("system:ossConfig:edit")
    @Log(title = "对象存储配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/updateSysOssConfig")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SysOssConfigReq bo) {
        return toAjax(ossConfigService.updateByBo(bo));
    }

    /**
     * 删除对象存储配置
     *
     * @param ossConfigIds OSS配置ID串
     */
    @SaCheckPermission("system:ossConfig:remove")
    @Log(title = "对象存储配置", businessType = BusinessType.DELETE)
    @PostMapping("/removeSysOssConfig")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @RequestParam("ids") String ossConfigIds) {
        List<Long> idList = Arrays.stream(ossConfigIds.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .map(Long::valueOf).toList();
        return toAjax(ossConfigService.deleteWithValidByIds(idList, true));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:ossConfig:edit")
    @Log(title = "对象存储状态修改", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PostMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysOssConfigReq bo) {
        return toAjax(ossConfigService.updateOssConfigStatus(bo));
    }
}
