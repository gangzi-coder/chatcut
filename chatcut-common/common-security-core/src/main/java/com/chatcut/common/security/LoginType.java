package com.chatcut.common.security;

/**
 * 三套独立的账号体系，token 互不通用：
 * C 端用户（user）、B 端企业用户（enterprise）与管理端运营（admin）
 * 分别注册独立的 StpLogic。
 */
public enum LoginType {

    /** C 端用户，token 有效期 7 天，由 StpUserUtil 操作。 */
    USER("user"),

    /** B 端企业用户，token 默认 12 小时（记住我 7 天），由 StpEnterpriseUtil 操作。 */
    ENTERPRISE("enterprise"),

    /** 管理端账号，token 有效期 2 小时（记住我 7 天），由 StpAdminUtil 操作。 */
    ADMIN("admin");

    /** sa-token 登录类型标识，与 StpLogicConfig 中注册的 StpLogic 一致。 */
    private final String value;

    LoginType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
