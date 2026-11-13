package com.chatcut.common.security.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.chatcut.common.core.domain.Result;
import com.chatcut.common.core.exception.CommonErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 鉴权异常出口，与网关 {@code AuthWebFilter#unauthorized} 保持同一套语义：
 * <ul>
 *   <li>未登录 / token 失效 → HTTP 401，业务码 10004；</li>
 *   <li>已登录但无权限（缺权限或角色）→ HTTP 403，业务码 10002。</li>
 * </ul>
 *
 * <p>之所以单独一个 advice 而不并进 {@code GlobalExceptionHandler}：后者的
 * {@code @ExceptionHandler(Exception.class)} 兜底会把未登录当成"未知异常"打成 ERROR 并回系统繁忙，
 * 既污染告警（前端 token 过期就刷一条堆栈），又让前端拿到 10999 无法跳登录。
 * 这里没有通用兜底，只认这三类异常，不会抢其他异常的归属。</p>
 *
 * <p>优先级设为最高，确保在通用兜底之前命中。未登录是高频正常事件，用 debug 记录且不带堆栈。</p>
 *
 * @see SaTokenWebConfig
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录 / token 无效 / 过期 / 被顶下线。
     * type 取值见 {@link NotLoginException} 的常量（notToken、tokenTimeout、beReplaced…），
     * loginType 区分是 user 还是 admin 体系，排查"串号"时有用。
     */
    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e) {
        log.debug("unauthorized: loginType={} type={} detail={}", e.getLoginType(), e.getType(), e.getMessage());
        return Result.fail(CommonErrorCode.UNAUTHORIZED);
    }

    /** 已登录但缺少权限或角色（@SaCheckPermission / @SaCheckRole 抛出）。 */
    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNoPermission(Exception e) {
        log.warn("forbidden: {}", e.getMessage());
        return Result.fail(CommonErrorCode.NO_PERMISSION);
    }
}
