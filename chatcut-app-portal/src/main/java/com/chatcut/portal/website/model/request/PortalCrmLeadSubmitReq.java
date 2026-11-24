package com.chatcut.portal.website.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 访客留资提交请求。
 * requestId + captchaToken 必填，企业类意向（ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM）
 * 强制填写联系人姓名与公司名称（服务端校验）。
 */
@Data
@Schema(description = "访客留资提交请求")
public class PortalCrmLeadSubmitReq implements Serializable {

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

    /** 公司名称，企业类意向必填。 */
    @Schema(description = "公司名称，intent 为企业类意向（ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM）时必填", example = "示例科技有限公司")
    @Size(max = 120, message = "公司名称长度不能超过 120")
    private String companyName;

    /** 联系人姓名，企业类意向必填。 */
    @Schema(description = "联系人姓名，intent 为企业类意向（ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM）时必填", example = "张三")
    @Size(max = 80, message = "联系人姓名长度不能超过 80")
    private String contactName;

    /** 联系方式明文：手机号或邮箱。 */
    @Schema(description = "联系方式明文，contactType=PHONE 时须为合法手机号、EMAIL 时须为合法邮箱", example = "13800138000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系方式不能为空")
    @Size(max = 200, message = "联系方式长度不能超过 200")
    private String contact;

    /** 联系方式类型：PHONE/EMAIL。 */
    @Schema(description = "联系方式类型", allowableValues = {"PHONE", "EMAIL"}, example = "PHONE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "联系方式类型不能为空")
    @Size(max = 16, message = "联系方式类型长度不能超过 16")
    private String contactType;

    /** 咨询意向：BETA_TRIAL/ENTERPRISE/API_TRIAL/TEAM_PLAN/CUSTOM。 */
    @Schema(description = "咨询意向：BETA_TRIAL=内测预约 / ENTERPRISE=企业合作 / API_TRIAL=API试用 / TEAM_PLAN=团队版咨询 / CUSTOM=定制接入；除 BETA_TRIAL 外均视为企业类意向（必填联系人+公司）",
        allowableValues = {"BETA_TRIAL", "ENTERPRISE", "API_TRIAL", "TEAM_PLAN", "CUSTOM"}, example = "BETA_TRIAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "咨询意向不能为空")
    @Size(max = 24, message = "咨询意向长度不能超过 24")
    private String intent;

    /** 留资来源页面/入口，前端传入，可空。 */
    @Schema(description = "留资来源页面/入口标识，可空", example = "home-hero")
    @Size(max = 200, message = "来源长度不能超过 200")
    private String source;

    /** 补充需求描述，可空。 */
    @Schema(description = "补充需求描述，可空", example = "希望了解 API 接入流程")
    @Size(max = 4000, message = "补充需求长度不能超过 4000")
    private String content;
}
