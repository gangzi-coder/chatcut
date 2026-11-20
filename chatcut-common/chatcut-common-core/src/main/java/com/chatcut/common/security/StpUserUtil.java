package com.chatcut.common.security;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpLogic;

/** C 端用户登录态。7 天滑动续期。 */
public final class StpUserUtil {

    /** 一天的秒数，仅用于换算 token 有效期。 */
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    /** C 端 token 有效期：7 天。 */
    private static final long USER_TIMEOUT_SECONDS = 7 * SECONDS_PER_DAY;

    private static volatile StpLogic logic;

    private StpUserUtil() {
    }

    static void bind(StpLogic stpLogic) {
        logic = stpLogic;
    }

    public static StpLogic logic() {
        if (logic == null) {
            throw new IllegalStateException("userStpLogic not initialized");
        }
        return logic;
    }

    public static void login(long userId) {
        logic().login(userId, SaLoginModel.create().setTimeout(USER_TIMEOUT_SECONDS));
    }

    public static void logout() {
        logic().logout();
    }

    public static boolean isLogin() {
        return logic().isLogin();
    }

    public static long userId() {
        return logic().getLoginIdAsLong();
    }

    public static Object userIdByToken(String token) {
        return logic().getLoginIdByToken(token);
    }
}
