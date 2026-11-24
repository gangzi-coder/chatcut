package com.chatcut.portal.website.service;

import com.chatcut.portal.website.support.CaptchaPair;

/**
 * 一次性验证码凭据服务：成对生成 requestId + captchaToken，不发送短信/邮件。
 */
public interface WebCaptchaTokenService {

    /**
     * 生成一次性验证码凭据对（requestId 签进令牌）。
     *
     * @return 凭据对（requestId + captchaToken，成对下发、成对使用）
     */
    CaptchaPair generateCaptchaPair();
}
