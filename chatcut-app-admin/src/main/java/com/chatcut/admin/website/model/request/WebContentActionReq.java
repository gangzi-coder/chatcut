package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内容动作请求体（发布 / 下线），主键由前端传 String。
 */
@Data
public class WebContentActionReq {

    /** 内容主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;
}
