package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.response.Result;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Web 异常响应工厂。
 *
 * <p>该工厂负责把 MVC 或 Filter 阶段捕获到的异常转换为统一的 {@link WebErrorResponse}，
 * 包括 HTTP 状态码和 {@link Result} 响应体。它是 GlobalExceptionHandler 与
 * SynapseExceptionBridgeFilter 之间复用响应规则的核心组件。</p>
 *
 * <p>本类只处理 Servlet MVC 栈，不包含 WebFlux / Gateway 异常类型。</p>
 */
public final class WebExceptionResponseFactory {

    /**
     * Servlet MVC 栈标识，用于区分后续可能出现的其他 Web 栈。
     */
    public static final String MVC_STACK = "mvc";

    private final CompositeErrorHttpStatusResolver statusResolver;

    public WebExceptionResponseFactory(CompositeErrorHttpStatusResolver statusResolver) {
        this.statusResolver = statusResolver;
    }

    /**
     * 按 MVC 栈规则转换异常。
     *
     * @param throwable 原始异常
     * @return Web 错误响应
     */
    public WebErrorResponse mvc(Throwable throwable) {
        return from(MVC_STACK, throwable);
    }

    /**
     * 按指定 Web 栈标识转换异常。
     *
     * @param stack Web 栈标识
     * @param throwable 原始异常
     * @return Web 错误响应
     */
    public WebErrorResponse from(String stack, Throwable throwable) {
        if (throwable instanceof SynapseException synapseException) {
            return business(stack, synapseException);
        }
        if (throwable instanceof MissingServletRequestParameterException
                || throwable instanceof MethodArgumentTypeMismatchException
                || throwable instanceof HttpMessageNotReadableException) {
            return validation(stack);
        }

        if (throwable instanceof NoHandlerFoundException
                || throwable instanceof NoResourceFoundException) {
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

        return error(stack, CommonErrorCode.COMMON_INTERNAL_ERROR,
                CommonErrorCode.COMMON_INTERNAL_ERROR.message());
    }

    /**
     * 创建请求参数错误响应。
     *
     * @param stack Web 栈标识
     * @return 400 错误响应
     */
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
