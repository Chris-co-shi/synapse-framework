package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebExceptionResponseFactoryTest {

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void shouldCreateSameBusinessResponseForMvcAndWebFlux() {
        TraceContext.setTraceId("trace-same");
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_CONFLICT, "版本冲突");

        WebErrorResponse mvc = WebExceptionResponseFactory.mvc(exception);
        WebErrorResponse webflux = WebExceptionResponseFactory.webflux(exception);

        assertEquals(409, mvc.status());
        assertEquals(409, webflux.status());
        assertEquals("mvc", mvc.stack());
        assertEquals("webflux", webflux.stack());
        assertEquals(mvc.body().getCode(), webflux.body().getCode());
        assertEquals(mvc.body().getMessage(), webflux.body().getMessage());
        assertEquals("trace-same", mvc.body().getTraceId());
        assertEquals("trace-same", webflux.body().getTraceId());
    }

    @Test
    void shouldCreateValidationResponseForBothStacks() {
        WebErrorResponse mvc = WebExceptionResponseFactory.validation(WebExceptionResponseFactory.MVC_STACK);
        WebErrorResponse webflux = WebExceptionResponseFactory.validation(WebExceptionResponseFactory.WEBFLUX_STACK);

        assertEquals(400, mvc.status());
        assertEquals(400, webflux.status());
        assertEquals("COMMON_BAD_REQUEST", mvc.body().getCode());
        assertEquals("COMMON_BAD_REQUEST", webflux.body().getCode());
    }

    @Test
    void shouldHideUnknownExceptionMessage() {
        WebErrorResponse response = WebExceptionResponseFactory.mvc(new IllegalStateException("database password leaked"));

        assertEquals(500, response.status());
        assertEquals("COMMON_INTERNAL_ERROR", response.body().getCode());
        assertEquals("系统内部错误", response.body().getMessage());
    }

    @Test
    void shouldMapMvcKnownHttpExceptions() {
        WebErrorResponse badRequest = WebExceptionResponseFactory.mvc(new MissingServletRequestParameterException("id", "String"));
        WebErrorResponse mismatch = WebExceptionResponseFactory.mvc(
                new MethodArgumentTypeMismatchException("x", String.class, "id", methodParameter(), null));
        WebErrorResponse methodNotAllowed = WebExceptionResponseFactory.mvc(new HttpRequestMethodNotSupportedException("POST"));
        WebErrorResponse unsupportedMediaType = WebExceptionResponseFactory.mvc(
                new HttpMediaTypeNotSupportedException("application/x-test"));

        assertEquals(400, badRequest.status());
        assertEquals("COMMON_BAD_REQUEST", badRequest.body().getCode());
        assertEquals(400, mismatch.status());
        assertEquals("COMMON_BAD_REQUEST", mismatch.body().getCode());
        assertEquals(405, methodNotAllowed.status());
        assertEquals("COMMON_METHOD_NOT_ALLOWED", methodNotAllowed.body().getCode());
        assertEquals(415, unsupportedMediaType.status());
        assertEquals("COMMON_UNSUPPORTED_MEDIA_TYPE", unsupportedMediaType.body().getCode());
    }

    private static MethodParameter methodParameter() {
        try {
            return new MethodParameter(WebExceptionResponseFactoryTest.class.getDeclaredMethod("helper", String.class), 0);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(exception);
        }
    }

    @SuppressWarnings("unused")
    private void helper(String value) {
    }
}
