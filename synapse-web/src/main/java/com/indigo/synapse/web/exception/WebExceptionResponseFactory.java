package com.indigo.synapse.web.exception;

import com.indigo.synapse.common.error.CommonErrorCode;
import com.indigo.synapse.common.error.ErrorCode;
import com.indigo.synapse.common.exception.BusinessException;
import com.indigo.synapse.web.response.ApiResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

public final class WebExceptionResponseFactory {

    public static final String MVC_STACK = "mvc";
    public static final String WEBFLUX_STACK = "webflux";

    private WebExceptionResponseFactory() {
    }

    public static WebErrorResponse mvc(Throwable throwable) {
        return from(MVC_STACK, throwable);
    }

    public static WebErrorResponse webflux(Throwable throwable) {
        return from(WEBFLUX_STACK, throwable);
    }

    public static WebErrorResponse from(String stack, Throwable throwable) {
        if (throwable instanceof BusinessException businessException) {
            return business(stack, businessException);
        }
        if (throwable instanceof MissingServletRequestParameterException
                || throwable instanceof MethodArgumentTypeMismatchException) {
            return validation(stack);
        }
        if (throwable instanceof ServerWebInputException) {
            return validation(stack);
        }
        if (throwable instanceof NoHandlerFoundException) {
            return error(stack, CommonErrorCode.COMMON_NOT_FOUND, CommonErrorCode.COMMON_NOT_FOUND.message());
        }
        if (throwable instanceof HttpRequestMethodNotSupportedException) {
            return error(stack, CommonErrorCode.COMMON_METHOD_NOT_ALLOWED,
                    CommonErrorCode.COMMON_METHOD_NOT_ALLOWED.message());
        }
        if (throwable instanceof HttpMediaTypeNotSupportedException) {
            return error(stack, CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE,
                    CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE.message());
        }
        if (throwable instanceof MethodNotAllowedException) {
            return error(stack, CommonErrorCode.COMMON_METHOD_NOT_ALLOWED,
                    CommonErrorCode.COMMON_METHOD_NOT_ALLOWED.message());
        }
        if (throwable instanceof UnsupportedMediaTypeStatusException) {
            return error(stack, CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE,
                    CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE.message());
        }
        return error(stack, CommonErrorCode.COMMON_INTERNAL_ERROR, CommonErrorCode.COMMON_INTERNAL_ERROR.message());
    }

    public static WebErrorResponse validation(String stack) {
        return error(stack, CommonErrorCode.COMMON_BAD_REQUEST, CommonErrorCode.COMMON_BAD_REQUEST.message());
    }

    private static WebErrorResponse business(String stack, BusinessException exception) {
        ErrorCode errorCode = exception.errorCode();
        return error(stack, errorCode, exception.getMessage());
    }

    private static WebErrorResponse error(String stack, ErrorCode errorCode, String message) {
        return new WebErrorResponse(stack, errorCode.httpStatus(), ApiResponse.fail(errorCode, message));
    }
}
