package com.indigo.synapse.web.response;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.web.trace.TraceContext;
import com.indigo.synapse.web.trace.TraceIdGenerator;

import java.time.Instant;
import java.util.Objects;

public final class Result<T> {

    private final String code;
    private final String message;
    private final T data;
    private final String traceId;
    private final Instant timestamp;

    private Result(String code, String message, T data, String traceId, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public static Result<Void> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.message(), data, traceId(), Instant.now());
    }

    public static Result<Void> fail(ErrorCode errorCode) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return fail(checkedErrorCode, checkedErrorCode.message());
    }

    public static Result<Void> fail(ErrorCode errorCode, String message) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return new Result<>(checkedErrorCode.code(), message, null, traceId(), Instant.now());
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTraceId() {
        return traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    private static String traceId() {
        return TraceContext.currentTraceId().orElseGet(TraceIdGenerator::generate);
    }
}
