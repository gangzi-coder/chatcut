package com.chatcut.common.security;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpLogic;

/** B 端企业用户登录态。默认 12 小时，勾选"记住我"时 7 天。 */
public final class StpEnterpriseUtil {

    /** 一天的秒数，仅用于换算 token 有效期。 */
    private static final long SECONDS_PER_DAY = 24 * 60 * 60;

    /** 一天的秒数（小时制）用于换算默认有效期。 */
    private static final long SECONDS_PER_HOUR = 60 * 60;

    /** B 端 token 默认有效期：12 小时（工作时间档，介于 C 端 7 天与后台 2 小时之间）。 */
    private static final long ENTERPRISE_TIMEOUT_SECONDS = 12 * SECONDS_PER_HOUR;

    /** 勾选"记住我"后的有效期：7 天。 */
    private static final long ENTERPRISE_REMEMBER_TIMEOUT_SECONDS = 7 * SECONDS_PER_DAY;

    private static volatile StpLogic logic;

    private StpEnterpriseUtil() {
    }

    static void bind(StpLogic stpLogic) {
        logic = stpLogic;
    }

    public static StpLogic logic() {
        if (logic == null) {
            throw new IllegalStateException("enterpriseStpLogic not initialized");
        }
        return logic;
    }

    public static void login(long enterpriseUserId, boolean rememberMe) {
        long timeout = rememberMe ? ENTERPRISE_REMEMBER_TIMEOUT_SECONDS : ENTERPRISE_TIMEOUT_SECONDS;
        logic().login(enterpriseUserId, SaLoginModel.create().setTimeout(timeout));
    }

    public static void logout() {
        logic().logout();
    }

    public static boolean isLogin() {
        return logic().isLogin();
    }

    public static long enterpriseUserId() {
        return logic().getLoginIdAsLong();
    }

    public static Object enterpriseUserIdByToken(String token) {
        return logic().getLoginIdByToken(token);
    }
}
