package com.chatcut.portal.website.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 一次性验证码凭据对。requestId 与 captchaToken 成对下发、成对使用，
 * 提交留资/反馈时须原样回传（requestId 已签进令牌，不可替换）。
 */
@Data
@AllArgsConstructor
@Schema(description = "一次性验证码凭据对（成对下发，成对使用，提交时原样回传）")
public class PortalCaptchaTokenResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 服务端生成的一次性请求标识，提交时回传，用于防重复提交。 */
    @Schema(description = "服务端生成的一次性请求标识（16 位随机），提交留资/反馈时须原样回传，防重复提交", example = "a1B2c3D4e5F6g7H8")
    private String requestId;

    /** 一次性验证码令牌（HMAC 签名，内嵌 requestId）。 */
    @Schema(description = "一次性验证码令牌（与 requestId 绑定，不可搭配其他 requestId 使用）", example = "MX0kZnRlc3QtcmVx.MacPayloadBase64url")
    private String captchaToken;
}
