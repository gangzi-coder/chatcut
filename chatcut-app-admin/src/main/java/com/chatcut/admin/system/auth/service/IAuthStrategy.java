package com.chatcut.admin.system.auth.service;


import com.chatcut.common.core.exception.ServiceException;
import com.chatcut.common.core.utils.SpringUtils;
import com.chatcut.admin.system.repository.entity.SysClientEntity;
import com.chatcut.admin.system.model.resp.SysClientResp;
import com.chatcut.admin.system.model.resp.LoginResp;

/**
 * 授权策略
 *
 * @author Michelle.Chung
 */
public interface IAuthStrategy {

    String BASE_NAME = "AuthStrategy";

    /**
     * 登录
     *
     * @param body      登录对象
     * @param client    授权管理视图对象
     * @param grantType 授权类型
     * @return 登录验证信息
     */
    static LoginResp login(String body, SysClientResp client, String grantType) {
        // 授权类型和客户端id
        String beanName = grantType + BASE_NAME;
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("授权类型不正确!");
        }
        IAuthStrategy instance = SpringUtils.getBean(beanName);
        return instance.login(body, client);
    }

    /**
     * 登录
     *
     * @param body   登录对象
     * @param client 授权管理视图对象
     * @return 登录验证信息
     */
    LoginResp login(String body, SysClientResp client);

}
