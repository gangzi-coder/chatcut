package com.chatcut.admin.website.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新建内容（含初始草稿）请求体。类型与分组一旦创建不可修改。
 */
@Data
public class WebContentCreateReq {

    /** 内容类型：PAGE/NOTICE/CASE/RESOURCE/POLICY/PRODUCT。 */
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    /** 内容分组。 */
    @NotBlank(message = "内容分组不能为空")
    @Size(max = 64, message = "内容分组长度不能超过{max}个字符")
    private String groupKey;

    /** 初始草稿标题。 */
    @NotBlank(message = "标题不能为空")
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

    /** 按钮目标：lead:意向 / content:内容ID / https://地址。 */
    @Size(max = 500, message = "按钮目标长度不能超过{max}个字符")
    private String ctaTarget;

    /** 关联素材 ID。 */
    private String mediaId;

    /** 开放状态：PREVIEW/AVAILABLE/PAUSED，默认 PREVIEW。 */
    private String openState;
}
