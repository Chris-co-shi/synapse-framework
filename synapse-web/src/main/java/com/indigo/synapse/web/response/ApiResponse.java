package com.indigo.synapse.web.response;

import com.indigo.synapse.common.error.CommonErrorCode;
import com.indigo.synapse.common.error.ErrorCode;
import com.indigo.synapse.web.trace.TraceContext;
import com.indigo.synapse.web.trace.TraceIdGenerator;

import java.time.Instant;
import java.util.Objects;

public final class ApiResponse<T> {

    private final String code;
    private final String message;
    private final T data;
    private final String traceId;
    private final Instant timestamp;

    private ApiResponse(String code, String message, T data, String traceId, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(CommonErrorCode.SUCCESS.code(), CommonErrorCode.SUCCESS.message(), data, traceId(), Instant.now());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return fail(checkedErrorCode, checkedErrorCode.message());
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        ErrorCode checkedErrorCode = requireErrorCode(errorCode);
        return new ApiResponse<>(checkedErrorCode.code(), message, null, traceId(), Instant.now());
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

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }

    private static String traceId() {
        return TraceContext.currentTraceId().orElseGet(TraceIdGenerator::generate);
    }
}
