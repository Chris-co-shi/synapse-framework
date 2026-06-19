package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.error.ErrorCode;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.core.error.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.web.core.response.Result;

/**
 * WebFlux 异常响应工厂。
 *
 * <p>该工厂只转换技术异常响应，不处理 Gateway 路由、鉴权业务或平台服务语义。</p>
 */
public final class WebFluxExceptionResponseFactory {

    private final CompositeErrorHttpStatusResolver statusResolver;

    public WebFluxExceptionResponseFactory(CompositeErrorHttpStatusResolver statusResolver) {
        this.statusResolver = statusResolver;
    }

    public WebFluxErrorResponse from(Throwable throwable, String traceId) {
        if (throwable instanceof SynapseException synapseException) {
            return business(synapseException, traceId);
        }
        return error(CommonErrorCode.COMMON_INTERNAL_ERROR, CommonErrorCode.COMMON_INTERNAL_ERROR.message(), traceId);
    }

    private WebFluxErrorResponse business(SynapseException exception, String traceId) {
        ErrorCode errorCode = exception.errorCode();
        if (exception instanceof SynapseAuthenticationException) {
            return new WebFluxErrorResponse(401, Result.fail(errorCode, exception.getMessage(), traceId));
        }
        if (exception instanceof SynapseAccessDeniedException) {
            return new WebFluxErrorResponse(403, Result.fail(errorCode, exception.getMessage(), traceId));
        }
        return error(errorCode, exception.getMessage(), traceId);
    }

    private WebFluxErrorResponse error(ErrorCode errorCode, String message, String traceId) {
        return new WebFluxErrorResponse(
                statusResolver.resolve(errorCode),
                Result.fail(errorCode, message, traceId)
        );
    }
}
