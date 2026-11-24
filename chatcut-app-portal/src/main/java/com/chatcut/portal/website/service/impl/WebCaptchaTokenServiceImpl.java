package com.chatcut.portal.website.service.impl;

import com.chatcut.portal.website.service.WebCaptchaTokenService;
import com.chatcut.portal.website.support.CaptchaPair;
import com.chatcut.portal.website.support.CaptchaTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 一次性验证码凭据服务实现。成对生成 requestId + captchaToken（requestId 签进令牌），
 * 不发送短信/邮件；每 IP 限流由 controller 层 @RateLimiter（Redis/Redisson）承担。
 */
@RequiredArgsConstructor
@Service
public class WebCaptchaTokenServiceImpl implements WebCaptchaTokenService {

    private final CaptchaTokenManager captchaTokenManager;

    @Override
    public CaptchaPair generateCaptchaPair() {
        return captchaTokenManager.generate();
    }
}
