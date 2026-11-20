package com.chatcut.admin.system.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.core.util.ObjectUtil;
import com.chatcut.admin.system.auth.service.IAuthStrategy;
import com.chatcut.admin.system.auth.service.SysLoginService;
import com.chatcut.admin.system.model.req.LoginReq;
import com.chatcut.admin.system.model.resp.LoginResp;
import com.chatcut.admin.system.model.resp.SysClientResp;
import com.chatcut.admin.system.service.ISysClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.chatcut.common.core.constant.SystemConstants;
import com.chatcut.common.core.domain.R;
import com.chatcut.common.core.utils.MessageUtils;
import com.chatcut.common.core.utils.StringUtils;
import com.chatcut.common.encrypt.annotation.ApiEncrypt;
import com.chatcut.common.json.utils.JsonUtils;
import com.chatcut.common.satoken.utils.LoginHelper;
import com.chatcut.common.sse.dto.SseMessageDto;
import com.chatcut.common.sse.utils.SseMessageUtils;
import com.chatcut.common.core.utils.DateUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 认证接口。
 *
 * <p>登录按若依约定分两步校验：先用 {@code sys_client} 校验 clientId 与 grantType，
 * 再交由对应的 {@link IAuthStrategy} 完成账号校验与令牌签发。</p>
 *
 * @author Lion Li
 */
@Slf4j
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
@Tag(name = "认证", description = "登录、退出登录；令牌为 Sa-Token JWT，后续请求需带 Authorization 与 clientid 请求头")
public class AuthController {

    /**
     * 登录服务。
     */
    private final SysLoginService loginService;

    /**
     * 客户端服务。
     */
    private final ISysClientService clientService;

    /**
     * 登录成功后延迟推送欢迎消息的调度器。
     */
    private final ScheduledExecutorService scheduledExecutorService;

    /**
     * 账号登录。
     *
     * <p>请求体为 JSON，字段见 {@link LoginReq}；接口加密开启时由过滤器解密后再绑定。
     * 首次调用前先到 {@code GET /auth/code} 获取验证码（{@code captcha.enable=false} 时可跳过）。</p>
     *
     * @param loginReq 登录参数
     * @return 登录令牌
     */
    @Operation(
        summary = "账号登录")
    @ApiEncrypt
    @PostMapping("/login")
    public R<LoginResp> login(
            @Parameter(hidden = true) @RequestHeader(value = "clientid", required = false) String clientid,
        @Validated @RequestBody LoginReq loginReq) {
        // 授权类型和客户端id
        String clientId = loginReq.getClientId();
        String grantType = loginReq.getGrantType();
        SysClientResp client = clientService.queryByClientId(clientId);
        // 查询不到 client 或 client 内不包含 grantType
        if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType)) {
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            return R.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            return R.fail(MessageUtils.message("auth.grant.type.blocked"));
        }

        // 请求头与请求体客户端标识需一致，避免网关按错误 clientid 解析令牌
        if (StringUtils.isNotBlank(clientid) && !StringUtils.equals(clientid, clientId)) {
            return R.fail("请求头 clientid 与请求体 clientId 不一致");
        }
        // 登录：策略层按 grantType 将登录参数解析为对应的 *LoginBody
        LoginResp loginVo = IAuthStrategy.login(JsonUtils.toJsonString(loginReq), client, grantType);
        Long userId = LoginHelper.getUserId();
        scheduledExecutorService.schedule(() -> {
            SseMessageDto dto = new SseMessageDto();
            dto.setMessage(DateUtils.getTodayHour(new Date()) + "好，欢迎登录 RuoYi-Vue-Plus 后台管理系统");
            dto.setUserIds(List.of(userId));
            SseMessageUtils.publishMessage(dto);
        }, 5, TimeUnit.SECONDS);
        return R.ok(loginVo);
    }

    /**
     * 退出登录。
     *
     * @return 操作结果
     */
    @Operation(summary = "退出登录", description = "注销当前令牌并清理在线会话")
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }
}
