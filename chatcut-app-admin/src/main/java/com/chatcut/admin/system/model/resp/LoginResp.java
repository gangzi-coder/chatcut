package com.chatcut.admin.system.model.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录验证信息
 *
 * @author Michelle.Chung
 */
@Data
@Schema(description = "登录令牌信息")
public class LoginResp {

    /**
     * 授权令牌
     */
    @Schema(description = "授权令牌（JWT），后续请求放在 Authorization: Bearer 中")
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌")
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * 授权令牌 access_token 的有效期
     */
    @Schema(description = "授权令牌有效期，单位秒")
    @JsonProperty("expire_in")
    private Long expireIn;

    /**
     * 刷新令牌 refresh_token 的有效期
     */
    @Schema(description = "刷新令牌有效期，单位秒")
    @JsonProperty("refresh_expire_in")
    private Long refreshExpireIn;

    /**
     * 应用id
     */
    @Schema(description = "客户端标识，与登录请求的 clientId 一致")
    @JsonProperty("client_id")
    private String clientId;

    /**
     * 令牌权限
     */
    private String scope;

    /**
     * 用户 openid
     */
    private String openid;

}
