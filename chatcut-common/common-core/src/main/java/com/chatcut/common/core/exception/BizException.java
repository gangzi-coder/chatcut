package com.chatcut.common.core.exception;

/**
 * 可预期的业务失败：不打 error 日志、不触发告警，原样返回给调用方。
 */
public class BizException extends RuntimeException {

    private final int code;
    private final transient ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.msg());
        this.errorCode = errorCode;
        this.code = errorCode.code();
    }

    public BizException(ErrorCode errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
        this.code = errorCode.code();
    }

    public int getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
