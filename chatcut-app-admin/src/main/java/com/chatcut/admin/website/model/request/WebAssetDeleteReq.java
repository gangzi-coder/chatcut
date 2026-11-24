package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 素材删除请求体（逻辑删除，保留 OSS 原文件）。
 */
@Data
public class WebAssetDeleteReq {

    /** 素材主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;
}
