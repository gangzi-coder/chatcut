package com.chatcut.portal.website.support;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一次性验证码凭据对：服务端成对下发（getWebCaptchaToken），
 * 访客提交留资/反馈时须原样回传，requestId 已密码学绑定进令牌签名。
 */
@Data
@AllArgsConstructor
public class CaptchaPair {

    /** 服务端生成的一次性请求标识（16 位随机），提交时回传，防重复提交。 */
    private String requestId;

    /** 一次性验证码令牌（HMAC 签名，内嵌 requestId），10 分钟有效。 */
    private String captchaToken;
}
