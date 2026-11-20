package com.chatcut.common.security;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;

/** 管理端登录态。默认 2 小时，勾选"记住我"时 7 天。 */
public final class StpAdminUtil {

    /** 一天的秒数，仅用于换算 token 有效期。 */
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    /** 一天的秒数（小时制）用于换算默认有效期。 */
    private static final long SECONDS_PER_HOUR = 60 * 60;

    /** 管理端 token 默认有效期：2 小时。 */
    private static final long ADMIN_TIMEOUT_SECONDS = 2 * SECONDS_PER_HOUR;

    /** 勾选"记住我"后的有效期：7 天。 */
    private static final long ADMIN_REMEMBER_TIMEOUT_SECONDS = 7 * SECONDS_PER_DAY;

    private static volatile StpLogic logic;

    private StpAdminUtil() {
    }

    static void bind(StpLogic stpLogic) {
        logic = stpLogic;
    }

    public static StpLogic logic() {
        // In the admin service RuoYi owns the default login StpLogic; in the gateway the
        // ChatCut registrar binds the same login type explicitly for token resolution.
        return logic == null ? StpUtil.getStpLogic() : logic;
    }

    public static void login(long adminId, boolean rememberMe) {
        long timeout = rememberMe ? ADMIN_REMEMBER_TIMEOUT_SECONDS : ADMIN_TIMEOUT_SECONDS;
        logic().login(adminId, SaLoginModel.create().setTimeout(timeout));
    }

    public static void logout() {
        logic().logout();
    }

    public static boolean isLogin() {
        return logic().isLogin();
    }

    public static long adminId() {
        return parseAdminId(logic().getLoginId());
    }

    public static Long adminIdDefaultNull() {
        Object loginId = logic().getLoginIdDefaultNull();
        return loginId == null ? null : parseAdminId(loginId);
    }

    public static Object adminIdByToken(String token) {
        Object loginId = logic().getLoginIdByToken(token);
        return loginId == null ? null : parseAdminId(loginId);
    }

    /** RuoYi loginId is encoded as {@code userType:userId}; ChatCut carries only the numeric id downstream. */
    private static long parseAdminId(Object loginId) {
        String value = String.valueOf(loginId);
        int separator = value.lastIndexOf(':');
        return Long.parseLong(separator >= 0 ? value.substring(separator + 1) : value);
    }
}
