package com.chatcut.portal.website.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 访客反馈提交请求。requestId + captchaToken 必填，反馈内容长度受限，初始状态 PENDING。
 */
@Data
@Schema(description = "访客反馈提交请求")
public class PortalCrmFeedbackSubmitReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 服务端下发的一次性请求标识（与 captchaToken 成对）。 */
    @Schema(description = "服务端一次性请求标识，GET /portal/website/getWebCaptchaToken 成对下发，须与 captchaToken 原样成对回传（已签进令牌，不可替换）", example = "a1B2c3D4e5F6g7H8", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "requestId 不能为空")
    @Size(max = 64, message = "requestId 长度不能超过 64")
    private String requestId;

    /** 通过 GET /portal/website/getWebCaptchaToken 获取的一次性验证码令牌。 */
    @Schema(description = "一次性验证码令牌，GET /portal/website/getWebCaptchaToken 与 requestId 成对下发，10 分钟有效，token 与 requestId 任一被用过即失效", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "captchaToken 不能为空")
    @Size(max = 64, message = "captchaToken 长度不能超过 64")
    private String captchaToken;

    /** 反馈类别：PAGE_ISSUE/USAGE_ISSUE/CONTENT_SUGGESTION/PARTNERSHIP/OTHER。 */
    @Schema(description = "反馈类别：PAGE_ISSUE=页面问题 / USAGE_ISSUE=使用问题 / CONTENT_SUGGESTION=内容建议 / PARTNERSHIP=合作咨询 / OTHER=其他",
        allowableValues = {"PAGE_ISSUE", "USAGE_ISSUE", "CONTENT_SUGGESTION", "PARTNERSHIP", "OTHER"}, example = "PAGE_ISSUE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈类别不能为空")
    @Size(max = 80, message = "反馈类别长度不能超过 80")
    private String category;

    /** 反馈内容。 */
    @Schema(description = "反馈内容（纯文本）", example = "首页产品卡片在移动端显示异常", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 4000, message = "反馈内容长度不能超过 4000")
    private String body;

    /** 联系方式明文，可空；填写时 contactType 必填且格式校验。 */
    @Schema(description = "联系方式明文，可空；填写时必须同时传 contactType", example = "user@example.com")
    @Size(max = 200, message = "联系方式长度不能超过 200")
    private String contact;

    /** 联系方式类型：PHONE/EMAIL，contact 填写时必填。 */
    @Schema(description = "联系方式类型，contact 填写时必填", allowableValues = {"PHONE", "EMAIL"}, example = "EMAIL")
    @Size(max = 16, message = "联系方式类型长度不能超过 16")
    private String contactType;

    /** 反馈来源页面/入口，前端传入，可空。 */
    @Schema(description = "反馈来源页面/入口标识，可空", example = "feedback-entry")
    @Size(max = 200, message = "来源长度不能超过 200")
    private String source;
}
