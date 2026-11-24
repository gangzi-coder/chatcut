package com.chatcut.admin.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.lock.annotation.Lock4j;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.excel.utils.ExcelUtil;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.web.core.BaseController;
import com.chatcut.admin.system.model.req.SysOperLogReq;
import com.chatcut.admin.system.model.resp.SysOperLogResp;
import com.chatcut.admin.system.service.ISysOperLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {

    private final ISysOperLogService operLogService;

    /**
     * 获取操作日志记录列表
     */
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/list")
    public TableDataInfo<SysOperLogResp> list(SysOperLogReq operLog, PageQuery pageQuery) {
        return operLogService.selectPageOperLogList(operLog, pageQuery);
    }

    /**
     * 导出操作日志记录列表
     */
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @SaCheckPermission("monitor:operlog:export")
    @PostMapping("/export")
    public void export(SysOperLogReq operLog, HttpServletResponse response) {
        List<SysOperLogResp> list = operLogService.selectOperLogList(operLog);
        ExcelUtil.exportExcel(list, "操作日志", SysOperLogResp.class, response);
    }

    /**
     * 批量删除操作日志记录
     * @param operIds 日志ids
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @SaCheckPermission("monitor:operlog:remove")
    @PostMapping("/removeSysOperLog")
    public R<Void> remove(@RequestParam("ids") String operIds) {
        List<Long> idList = Arrays.stream(operIds.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .map(Long::valueOf).toList();
        return toAjax(operLogService.deleteOperLogByIds(idList.toArray(new Long[0])));
    }

    /**
     * 清理操作日志记录
     */
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @SaCheckPermission("monitor:operlog:remove")
    @Lock4j
    @PostMapping("/cleanSysOperLog")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
