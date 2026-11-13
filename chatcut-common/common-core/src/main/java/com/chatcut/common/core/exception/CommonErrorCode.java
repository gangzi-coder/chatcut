package com.chatcut.common.core.exception;

public enum CommonErrorCode implements ErrorCode {

    PARAM_INVALID(10001, "参数不合法"),
    NO_PERMISSION(10002, "没有权限"),
    RATE_LIMITED(10003, "请求过于频繁，请稍后再试"),
    UNAUTHORIZED(10004, "登录已失效，请重新登录"),
    IDEMPOTENT_CONFLICT(10005, "请求正在处理中，请勿重复提交"),
    NOT_FOUND(10006, "资源不存在"),
    SYSTEM_ERROR(10999, "系统繁忙，请稍后再试");

    private final int code;
    private final String msg;

    CommonErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String msg() {
        return msg;
    }
}
