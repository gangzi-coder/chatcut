package com.chatcut.admin.system.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.Constants;
import com.chatcut.common.core.constant.GlobalConstants;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.model.LoginUser;
import com.chatcut.common.core.domain.model.SmsLoginBody;
import com.chatcut.common.core.enums.LoginType;
import com.chatcut.common.core.exception.user.CaptchaExpireException;
import com.chatcut.common.core.exception.user.UserException;
import com.chatcut.common.core.utils.MessageUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.core.utils.ValidatorUtils;
import com.chatcut.common.json.utils.JsonUtils;
import com.chatcut.common.redis.utils.RedisUtils;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.common.tenant.helper.TenantHelper;
import com.chatcut.admin.system.repository.entity.SysUserEntity;
import com.chatcut.admin.system.model.resp.SysClientResp;
import com.chatcut.admin.system.model.resp.SysUserResp;
import com.chatcut.admin.system.repository.mapper.SysUserMapper;
import com.chatcut.admin.system.model.resp.LoginResp;
import com.chatcut.admin.system.auth.service.IAuthStrategy;
import com.chatcut.admin.system.auth.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 短信认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("sms" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class SmsAuthStrategy implements IAuthStrategy {

    private final SysLoginService loginService;
    private final SysUserMapper userMapper;

    @Override
    public LoginResp login(String body, SysClientResp client) {
        SmsLoginBody loginBody = JsonUtils.parseObject(body, SmsLoginBody.class);
        ValidatorUtils.validate(loginBody);
        String tenantId = loginBody.getTenantId();
        String phonenumber = loginBody.getPhonenumber();
        String smsCode = loginBody.getSmsCode();
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            SysUserResp user = loadUserByPhonenumber(phonenumber);
            loginService.checkLogin(LoginType.SMS, tenantId, user.getUserName(), () -> !validateSmsCode(tenantId, phonenumber, smsCode));
            // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
            return loginService.buildLoginUser(user);
        });
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginParameter model = new SaLoginParameter();
        model.setDeviceType(client.getDeviceType());
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginResp loginVo = new LoginResp();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    /**
     * 校验短信验证码
     */
    private boolean validateSmsCode(String tenantId, String phonenumber, String smsCode) {
        String code = RedisUtils.getCacheObject(GlobalConstants.CAPTCHA_CODE_KEY + phonenumber);
        if (StringUtils.isBlank(code)) {
            loginService.recordLogininfor(tenantId, phonenumber, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        return code.equals(smsCode);
    }

    private SysUserResp loadUserByPhonenumber(String phonenumber) {
        SysUserResp user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getPhonenumber, phonenumber));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", phonenumber);
            throw new UserException("user.not.exists", phonenumber);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", phonenumber);
            throw new UserException("user.blocked", phonenumber);
        }
        return user;
    }

}
