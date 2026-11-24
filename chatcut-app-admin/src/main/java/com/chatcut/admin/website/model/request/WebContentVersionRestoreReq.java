package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 内容历史版本恢复请求体，恢复只写回草稿、仍需再次发布。
 */
@Data
public class WebContentVersionRestoreReq {

    /** 内容主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;

    /** 目标历史版本号。 */
    @NotNull(message = "版本号不能为空")
    private Integer versionNo;
}
