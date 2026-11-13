package com.chatcut.common.core.exception;

/**
 * 错误码契约。实现方：各域枚举（10xxx 通用 / 20xxx 认证 / 30xxx 用户 ...）。
 */
public interface ErrorCode {

    int code();

    String msg();
}
