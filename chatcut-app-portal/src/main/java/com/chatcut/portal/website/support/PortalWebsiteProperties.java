package com.chatcut.portal.website.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * portal 官网域配置属性（Nacos chatcut-app-portal-local.yaml 中 chatcut.website 段）。
 * 全部字段带默认值，便于本地裸跑。访客接口限流走 @RateLimiter（Redis/Redisson）注解，
 * 阈值为静态值，不在本属性类中配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatcut.website")
public class PortalWebsiteProperties {

    /** 一次性验证码令牌配置（无 Redis 会话的 HMAC 无状态方案）。 */
    private final Captcha captcha = new Captcha();

    @Data
    public static class Captcha {

        /** HMAC-SHA256 签名密钥；生产环境必须在 Nacos 覆盖为强随机值。 */
        private String secretKey = "chatcut-portal-captcha-dev-secret-change-me";

        /** 令牌有效期（分钟）。 */
        private int ttlMinutes = 10;
    }
}
