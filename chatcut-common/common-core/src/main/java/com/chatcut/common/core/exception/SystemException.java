package com.chatcut.common.core.exception;

/**
 * 不可预期的系统异常：打 error 日志并告警，对外统一成"系统繁忙"。
 */
public class SystemException extends RuntimeException {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
