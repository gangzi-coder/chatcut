package com.chatcut.admin.system.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.chatcut.common.core.domain.model.LoginBody;

/**
 * 登录请求参数。
 *
 * <p>继承若依 {@link LoginBody}（含 clientId / grantType / tenantId / code / uuid），
 * 补充各授权类型的登录字段；登录接口直接以该类型绑定请求体，
 * 策略层按 grantType 二次解析为对应的 *LoginBody。</p>
 *
 * @author chatcut
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录请求参数")
public class LoginReq extends LoginBody {

    /**
     * 登录账号（grantType=password 时必填）。
     */
    @Schema(description = "登录账号，grantType=password 时必填", example = "admin")
    private String username;

    /**
     * 登录密码（grantType=password 时必填；接口加密开启时传密文）。
     */
    @Schema(description = "登录密码，grantType=password 时必填；接口加密开启时传密文", example = "admin123")
    private String password;

    /**
     * 手机号（grantType=sms 时必填）。
     */
    @Schema(description = "手机号，grantType=sms 时必填", example = "13800000000")
    private String phonenumber;

    /**
     * 短信验证码（grantType=sms 时必填）。
     */
    @Schema(description = "短信验证码，grantType=sms 时必填", example = "1234")
    private String smsCode;

    /**
     * 邮箱地址（grantType=email 时必填）。
     */
    @Schema(description = "邮箱地址，grantType=email 时必填", example = "admin@chatcut.cn")
    private String email;

    /**
     * 邮箱验证码（grantType=email 时必填）。
     */
    @Schema(description = "邮箱验证码，grantType=email 时必填", example = "1234")
    private String emailCode;

    /**
     * 校验请求体客户端标识必填。
     *
     * <p>Spring 校验只覆盖本类字段，clientId 定义在父类，
     * 此处显式补充非空校验。</p>
     *
     * @return 客户端标识
     */
    @NotBlank(message = "客户端ID不能为空")
    @Override
    public String getClientId() {
        return super.getClientId();
    }
}
