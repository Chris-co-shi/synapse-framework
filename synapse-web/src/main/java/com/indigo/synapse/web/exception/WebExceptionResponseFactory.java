package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.response.Result;
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

    private final CompositeErrorHttpStatusResolver statusResolver;

    public WebExceptionResponseFactory(CompositeErrorHttpStatusResolver statusResolver) {
        this.statusResolver = statusResolver;
    }

    public WebErrorResponse mvc(Throwable throwable) {
        return from(MVC_STACK, throwable);
    }

    public WebErrorResponse from(String stack, Throwable throwable) {
        if (throwable instanceof SynapseException synapseException) {
            return business(stack, synapseException);
        }

        if (throwable instanceof MissingServletRequestParameterException
                || throwable instanceof MethodArgumentTypeMismatchException
                || throwable instanceof ServerWebInputException) {
            return validation(stack);
        }

        if (throwable instanceof NoHandlerFoundException) {
            return error(stack, CommonErrorCode.COMMON_NOT_FOUND, CommonErrorCode.COMMON_NOT_FOUND.message());
        }

        if (throwable instanceof HttpRequestMethodNotSupportedException
                || throwable instanceof MethodNotAllowedException) {
            return error(stack, CommonErrorCode.COMMON_METHOD_NOT_ALLOWED,
                    CommonErrorCode.COMMON_METHOD_NOT_ALLOWED.message());
        }

        if (throwable instanceof HttpMediaTypeNotSupportedException
                || throwable instanceof UnsupportedMediaTypeStatusException) {
            return error(stack, CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE,
                    CommonErrorCode.COMMON_UNSUPPORTED_MEDIA_TYPE.message());
        }

        return error(stack, CommonErrorCode.COMMON_INTERNAL_ERROR,
                CommonErrorCode.COMMON_INTERNAL_ERROR.message());
    }

    public WebErrorResponse validation(String stack) {
        return error(stack, CommonErrorCode.COMMON_BAD_REQUEST,
                CommonErrorCode.COMMON_BAD_REQUEST.message());
    }

    private WebErrorResponse business(String stack, SynapseException exception) {
        ErrorCode errorCode = exception.errorCode();

        if (exception instanceof SynapseAuthenticationException) {
            return new WebErrorResponse(
                    stack,
                    401,
                    Result.fail(errorCode, exception.getMessage())
            );
        }

        if (exception instanceof SynapseAccessDeniedException) {
            return new WebErrorResponse(
                    stack,
                    403,
                    Result.fail(errorCode, exception.getMessage())
            );
        }

        return error(stack, errorCode, exception.getMessage());
    }

    private WebErrorResponse error(String stack, ErrorCode errorCode, String message) {
        return new WebErrorResponse(
                stack,
                statusResolver.resolve(errorCode),
                Result.fail(errorCode, message)
        );
    }
}