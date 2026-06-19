package com.indigo.synapse.webflux.response;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.webflux.trace.TraceIdGenerator;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * WebFlux 统一接口响应结果。
 *
 * <p>该类型只表达响应结构，不绑定 Gateway、Controller 或路由配置。WebFlux 异常处理器写出响应时
 * 应显式传入当前 reactive 链路中的 traceId，避免依赖 Servlet ThreadLocal。</p>
 */
public record Result<T>(
        String code,
        String message,
        T data,
        String traceId,
        Instant timestamp
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public Result {
        code = Objects.requireNonNull(code, "code must not be null");
        message = Objects.requireNonNullElse(message, "");
        traceId = Objects.requireNonNullElseGet(traceId, TraceIdGenerator::generate);
        timestamp = Objects.requireNonNullElseGet(timestamp, Instant::now);
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return success(data, TraceIdGenerator.generate());
    }

    public static <T> Result<T> success(T data, String traceId) {
        return new Result<>(
                CommonErrorCode.SUCCESS.code(),
                CommonErrorCode.SUCCESS.message(),
                data,
                traceId,
                Instant.now()
        );
    }

    public static Result<Void> fail(ErrorCode errorCode) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return fail(checkedErrorCode, checkedErrorCode.message(), TraceIdGenerator.generate());
    }

    public static Result<Void> fail(ErrorCode errorCode, String message) {
        return fail(errorCode, message, TraceIdGenerator.generate());
    }

    public static Result<Void> fail(ErrorCode errorCode, String message, String traceId) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return new Result<>(
                checkedErrorCode.code(),
                Objects.requireNonNullElse(message, checkedErrorCode.message()),
                null,
                traceId,
                Instant.now()
        );
    }

    public boolean isSuccess() {
        return CommonErrorCode.SUCCESS.code().equals(code);
    }

    public boolean isFailed() {
        return !isSuccess();
    }

    public Result<T> withTraceId(String traceId) {
        return new Result<>(code, message, data, traceId, timestamp);
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
