package com.chatcut.admin.system.controller.system;


import cn.dev33.satoken.annotation.SaCheckPermission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.validate.QueryGroup;
import com.chatcut.common.log.annotation.Log;
import com.chatcut.common.log.enums.BusinessType;
import com.chatcut.common.mybatis.core.page.PageQuery;
import com.chatcut.common.mybatis.core.page.TableDataInfo;
import com.chatcut.common.web.core.BaseController;
import com.chatcut.admin.system.model.req.SysOssReq;
import com.chatcut.admin.system.model.resp.SysOssUploadResp;
import com.chatcut.admin.system.model.resp.SysOssResp;
import com.chatcut.admin.system.service.ISysOssService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传 控制层
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/resource/oss")
public class SysOssController extends BaseController {

    private final ISysOssService ossService;

    /**
     * 查询OSS对象存储列表
     */
    @SaCheckPermission("system:oss:list")
    @GetMapping("/list")
    public TableDataInfo<SysOssResp> list(@Validated(QueryGroup.class) SysOssReq bo, PageQuery pageQuery) {
        return ossService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询OSS对象基于id串
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:query")
    @GetMapping("/listSysOssByIds")
    public R<List<SysOssResp>> listByIds(@NotEmpty(message = "主键不能为空")
                                       @RequestParam("ids") String ossIds) {
        List<Long> idList = Arrays.stream(ossIds.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .map(Long::valueOf).toList();
        List<SysOssResp> list = ossService.listByIds(idList);
        return R.ok(list);
    }

    /**
     * 上传OSS对象存储
     *
     * @param file 文件
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<SysOssUploadResp> upload(@RequestPart("file") MultipartFile file) {
        SysOssResp oss = ossService.upload(file);
        SysOssUploadResp uploadVo = new SysOssUploadResp();
        uploadVo.setUrl(oss.getUrl());
        uploadVo.setFileName(oss.getOriginalName());
        uploadVo.setOssId(oss.getOssId().toString());
        return R.ok(uploadVo);
    }

    /**
     * 下载OSS对象
     *
     * @param ossId OSS对象ID
     */
    @SaCheckPermission("system:oss:download")
    @GetMapping("/downloadSysOss")
    public void download(@RequestParam("ossId") String ossId, HttpServletResponse response) throws IOException {
        ossService.download(Long.valueOf(ossId), response);
    }

    /**
     * 删除OSS对象存储
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:remove")
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    @PostMapping("/removeSysOss")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @RequestParam("ids") String ossIds) {
        List<Long> idList = Arrays.stream(ossIds.split(","))
            .map(String::trim).filter(s -> !s.isEmpty())
            .map(Long::valueOf).toList();
        return toAjax(ossService.deleteWithValidByIds(idList, true));
    }

}
