package com.chatcut.common.security.web;

import com.chatcut.common.security.StpAdminUtil;
import com.chatcut.common.security.StpUserUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 取当前身份。业务代码只在 controller 层取一次，往下传参，不要在深层调用里到处取。
 *
 * <p>取值顺序：</p>
 * <ol>
 *   <li>优先读网关注入的 {@code X-User-Id} / {@code X-Admin-Id}（生产链路，带 HMAC 签名，信任度最高）；</li>
 *   <li>header 缺失时回退到 Sa-Token 当前请求的 loginId：本地直连服务端口（IDEA 起 8082、Swagger 联调）
 *       没有网关，header 必然为空，而登录态已由 {@code SaTokenWebConfig} 的拦截器校验过，取 token 里的身份是安全的。
 *       不回退会导致「token 有效但 adminId 为 null」→ 查不到人 → 回 10004，看起来像 token 失效，极难排查。</li>
 * </ol>
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /** 当前 C 端用户 ID；未登录返回 null。 */
    public static Long userId() {
        Long fromHeader = parse(header(InternalSignFilter.H_USER));
        if (fromHeader != null) {
            return fromHeader;
        }
        Object loginId = StpUserUtil.logic().getLoginIdDefaultNull();
        return loginId == null ? null : Long.valueOf(loginId.toString());
    }

    /** 当前管理端管理员 ID；未登录返回 null。 */
    public static Long adminId() {
        Long fromHeader = parse(header(InternalSignFilter.H_ADMIN));
        if (fromHeader != null) {
            return fromHeader;
        }
        Object loginId = StpAdminUtil.logic().getLoginIdDefaultNull();
        return loginId == null ? null : Long.valueOf(loginId.toString());
    }

    private static Long parse(String raw) {
        return raw == null ? null : Long.valueOf(raw);
    }

    private static String header(String name) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        String value = attrs.getRequest().getHeader(name);
        return value == null || value.isBlank() ? null : value;
    }
}
