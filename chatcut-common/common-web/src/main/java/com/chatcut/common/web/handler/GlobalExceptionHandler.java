package com.chatcut.common.web.handler;

import com.chatcut.common.core.domain.Result;
import com.chatcut.common.core.exception.BizException;
import com.chatcut.common.core.exception.CommonErrorCode;
import com.chatcut.common.core.exception.SystemException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常出口。原则：
 * 1) 业务失败用 BizException，warn 级日志，HTTP 200 + 业务码（调用方按 code 分支）；
 * 2) 系统级异常（SystemException / 未知异常）返回 HTTP 500 + 10999，error 级日志（触发告警），
 *    对外只给"系统繁忙" + traceId。HTTP 状态码保持真实：网关/监控/日志按状态码统计时，
 *    框架级故障不会被淹没在 200 里；
 * 3) 参数类异常统一映射 10001（HTTP 200），并把第一条字段错误作为提示。
 *
 * <p>优先级设为最低：本类带 {@code Exception} 兜底，必须排在所有专用 advice 之后，
 * 否则会把鉴权异常等"有明确语义"的异常一并当成未知异常。</p>
 */
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("biz failed code={} msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Void>> handleSystem(SystemException e) {
        log.error("system exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(CommonErrorCode.SYSTEM_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(CommonErrorCode.PARAM_INVALID, msg.isBlank() ? CommonErrorCode.PARAM_INVALID.msg() : msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(CommonErrorCode.PARAM_INVALID, msg.isBlank() ? CommonErrorCode.PARAM_INVALID.msg() : msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(CommonErrorCode.PARAM_INVALID, msg.isBlank() ? CommonErrorCode.PARAM_INVALID.msg() : msg);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, HttpMessageNotReadableException.class})
    public Result<Void> handleBadRequest(Exception e) {
        log.warn("bad request: {}", e.getMessage());
        return Result.fail(CommonErrorCode.PARAM_INVALID);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception e) {
        log.error("unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(CommonErrorCode.SYSTEM_ERROR));
    }
}
