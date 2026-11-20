package com.chatcut.admin.system.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.Constants;
import com.chatcut.common.core.constant.GlobalConstants;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.SpringUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.mail.config.properties.MailProperties;
import com.chatcut.common.mail.utils.MailUtils;
import com.chatcut.common.ratelimiter.annotation.RateLimiter;
import com.chatcut.common.ratelimiter.enums.LimitType;
import com.chatcut.common.redis.utils.RedisUtils;
import com.chatcut.common.web.core.WaveAndCircleCaptcha;
import com.chatcut.common.web.config.properties.CaptchaProperties;
import com.chatcut.admin.system.model.resp.CaptchaResp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.time.Duration;
import java.util.LinkedHashMap;

/**
 * 验证码操作处理
 *
 * @author Lion Li
 */
@SaIgnore
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@Tag(name = "认证", description = "登录验证码：图形验证码、邮箱验证码")
public class CaptchaController {

    private final CaptchaProperties captchaProperties;
    private final MailProperties mailProperties;

    /**
     * 邮箱验证码
     *
     * @param email 邮箱
     */
    @Operation(summary = "发送邮箱验证码", description = "需在配置中开启邮件功能")
    @GetMapping("/resource/email/code")
    public R<Void> emailCode(
        @Parameter(description = "邮箱地址", required = true, example = "admin@chatcut.cn")
        @NotBlank(message = "{user.email.not.blank}") String email) {
        if (!mailProperties.getEnabled()) {
            return R.fail("当前系统没有开启邮箱功能！");
        }
        SpringUtils.getAopProxy(this).emailCodeImpl(email);
        return R.ok();
    }

    /**
     * 邮箱验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    @RateLimiter(key = "#email", time = 60, count = 1)
    public void emailCodeImpl(String email) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + email;
        String code = RandomUtil.randomNumbers(4);
        RedisUtils.setCacheObject(key, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        try {
            MailUtils.sendText(email, "登录验证码", "您本次验证码为：" + code + "，有效性为" + Constants.CAPTCHA_EXPIRATION + "分钟，请尽快填写。");
        } catch (Exception e) {
            log.error("验证码邮件发送异常 => {}", e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 生成验证码
     */
    @Operation(summary = "获取图形验证码",
        description = "返回 uuid 与 base64 图片；captcha.enable=false 时 captchaEnabled 为 false，登录无需验证码")
    @GetMapping("/auth/code")
    public R<CaptchaResp> getCode() {
        boolean captchaEnabled = captchaProperties.getEnable();
        if (!captchaEnabled) {
            CaptchaResp captchaVo = new CaptchaResp();
            captchaVo.setCaptchaEnabled(false);
            return R.ok(captchaVo);
        }
        return R.ok(SpringUtils.getAopProxy(this).getCodeImpl());
    }

    /**
     * 生成验证码
     * 独立方法避免验证码关闭之后仍然走限流
     */
    @RateLimiter(time = 60, count = 10, limitType = LimitType.IP)
    public CaptchaResp getCodeImpl() {
        // 保存验证码信息
        String uuid = IdUtil.simpleUUID();
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + uuid;
        // 生成验证码
        String captchaType = captchaProperties.getType();
        CodeGenerator codeGenerator;
        if ("math".equals(captchaType)) {
            codeGenerator = new MathGenerator(captchaProperties.getNumberLength(), false);
        } else {
            codeGenerator = new RandomGenerator(captchaProperties.getCharLength());
        }
        WaveAndCircleCaptcha captcha = new WaveAndCircleCaptcha(160, 60);
        // captcha.setBackground(Color.WHITE); // 不设置就是透明底
        captcha.setFont(new Font("Arial", Font.BOLD, 45));
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        // 如果是数学验证码，使用SpEL表达式处理验证码结果
        String code = captcha.getCode();
        if ("math".equals(captchaType)) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        CaptchaResp captchaVo = new CaptchaResp();
        captchaVo.setUuid(uuid);
        captchaVo.setImg(captcha.getImageBase64());
        return captchaVo;
    }

}
