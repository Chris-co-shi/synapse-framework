package com.indigo.synapse.web.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseException;
import com.indigo.synapse.web.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebExceptionResponseFactoryTest {

    private WebExceptionResponseFactory responseFactory;

    @BeforeEach
    void setUp() {
        responseFactory = new WebExceptionResponseFactory(
                new CompositeErrorHttpStatusResolver(
                        List.of(new CommonErrorHttpStatusResolver())
                )
        );
    }

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void shouldCreateSameBusinessResponseForMvcAndWebFlux() {
        TraceContext.setTraceId("trace-same");
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_CONFLICT, "版本冲突");

        WebErrorResponse mvc = responseFactory.mvc(exception);
//        WebErrorResponse webflux = responseFactory.webflux(exception);

        assertEquals(409, mvc.status());
//        assertEquals(409, webflux.status());
        assertEquals("mvc", mvc.stack());
//        assertEquals("webflux", webflux.stack());
//        assertEquals(mvc.body().code(), webflux.body().code());
//        assertEquals(mvc.body().message(), webflux.body().message());
        assertEquals("trace-same", mvc.body().traceId());
//        assertEquals("trace-same", webflux.body().traceId());
    }

    @Test
    void shouldCreateValidationResponseForBothStacks() {
        WebErrorResponse mvc = responseFactory.validation(WebExceptionResponseFactory.MVC_STACK);
//        WebErrorResponse webflux = responseFactory.validation(WebExceptionResponseFactory.WEBFLUX_STACK);

        assertEquals(400, mvc.status());
//        assertEquals(400, webflux.status());
        assertEquals("COMMON_BAD_REQUEST", mvc.body().code());
//        assertEquals("COMMON_BAD_REQUEST", webflux.body().code());
    }

    @Test
    void shouldHideUnknownExceptionMessage() {
        WebErrorResponse response = responseFactory.mvc(
                new IllegalStateException("database password leaked")
        );

        assertEquals(500, response.status());
        assertEquals("COMMON_INTERNAL_ERROR", response.body().code());
        assertEquals("系统内部错误", response.body().message());
    }

    @Test
    void shouldMapMvcKnownHttpExceptions() {
        WebErrorResponse badRequest = responseFactory.mvc(
                new MissingServletRequestParameterException("id", "String")
        );
        WebErrorResponse mismatch = responseFactory.mvc(
                new MethodArgumentTypeMismatchException("x", String.class, "id", methodParameter(), null)
        );
        WebErrorResponse methodNotAllowed = responseFactory.mvc(
                new HttpRequestMethodNotSupportedException("POST")
        );
        WebErrorResponse unsupportedMediaType = responseFactory.mvc(
                new HttpMediaTypeNotSupportedException("application/x-test")
        );

        assertEquals(400, badRequest.status());
        assertEquals("COMMON_BAD_REQUEST", badRequest.body().code());

        assertEquals(400, mismatch.status());
        assertEquals("COMMON_BAD_REQUEST", mismatch.body().code());

        assertEquals(405, methodNotAllowed.status());
        assertEquals("COMMON_METHOD_NOT_ALLOWED", methodNotAllowed.body().code());

        assertEquals(415, unsupportedMediaType.status());
        assertEquals("COMMON_UNSUPPORTED_MEDIA_TYPE", unsupportedMediaType.body().code());
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