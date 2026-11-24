package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新内容草稿请求体（全量覆盖草稿字段）。保存草稿不影响官网展示。
 */
@Data
public class WebContentDraftReq {

    /** 内容主键，前端传 String。 */
    @NotBlank(message = "主键不能为空")
    private String id;

    /** 标题。 */
    @Size(max = 120, message = "标题长度不能超过{max}个字符")
    private String title;

    /** 摘要，发布前必填。 */
    @Size(max = 600, message = "摘要长度不能超过{max}个字符")
    private String summary;

    /** 正文，纯文本。 */
    private String body;

    /** 按钮文案，与 ctaTarget 成对。 */
    @Size(max = 40, message = "按钮文案长度不能超过{max}个字符")
    private String ctaLabel;

    /** 按钮目标。 */
    @Size(max = 500, message = "按钮目标长度不能超过{max}个字符")
    private String ctaTarget;

    /** 关联素材 ID。 */
    private String mediaId;

    /** 开放状态：PREVIEW/AVAILABLE/PAUSED。 */
    @NotBlank(message = "开放状态不能为空")
    private String openState;
}
