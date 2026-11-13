package com.chatcut.common.core.domain;

import com.chatcut.common.core.exception.ErrorCode;
import com.chatcut.common.core.trace.TraceContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 统一响应体。前端只需要判断 code == 0。
 */
@Getter
@Schema(description = "统一响应体")
public class Result<T> {

    public static final int SUCCESS = 0;

    @Schema(description = "业务状态码，0=成功，其余见错误码分段表", example = "0")
    private final int code;

    @Schema(description = "提示信息", example = "ok")
    private final String msg;

    @Schema(description = "业务数据，失败时为 null")
    private final T data;

    @Schema(description = "全链路追踪 id，与响应头 X-Trace-Id 一致，排查问题时提供给后端")
    private final String traceId;

    private Result(int code, String msg, T data, String traceId) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.traceId = traceId;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, "ok", data, TraceContext.get());
    }

    public static Result<Void> ok() {
        return new Result<>(SUCCESS, "ok", null, TraceContext.get());
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.code(), errorCode.msg(), null, TraceContext.get());
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String msg) {
        return new Result<>(errorCode.code(), msg, null, TraceContext.get());
    }

    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null, TraceContext.get());
    }

    public boolean succeeded() {
        return code == SUCCESS;
    }
}
