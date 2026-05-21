package com.indigo.synapse.common.exception;

import com.indigo.synapse.common.error.ErrorCode;

import java.util.Objects;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, requireErrorCode(errorCode).message());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = requireErrorCode(errorCode);
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    private static ErrorCode requireErrorCode(ErrorCode errorCode) {
        return Objects.requireNonNull(errorCode, "errorCode must not be null");
    }
}
