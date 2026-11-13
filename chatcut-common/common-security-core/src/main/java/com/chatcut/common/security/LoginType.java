package com.chatcut.common.security;

/**
 * 两套独立的账号体系，token 互不通用：
 * C 端用户（user）与管理端运营（admin）分别注册独立的 StpLogic。
 */
public enum LoginType {

    /** C 端用户，token 有效期 7 天，由 StpUserUtil 操作。 */
    USER("user"),

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
