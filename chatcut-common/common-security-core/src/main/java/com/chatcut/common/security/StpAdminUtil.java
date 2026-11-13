package com.chatcut.common.security;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpLogic;

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
        if (logic == null) {
            throw new IllegalStateException("adminStpLogic not initialized");
        }
        return logic;
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
        return logic().getLoginIdAsLong();
    }

    public static Object adminIdByToken(String token) {
        return logic().getLoginIdByToken(token);
    }
}
