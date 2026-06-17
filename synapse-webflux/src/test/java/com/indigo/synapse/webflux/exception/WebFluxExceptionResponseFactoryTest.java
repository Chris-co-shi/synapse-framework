package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebFluxExceptionResponseFactoryTest {

    private WebFluxExceptionResponseFactory responseFactory;

    @BeforeEach
    void setUp() {
        responseFactory = new WebFluxExceptionResponseFactory(
                new CompositeErrorHttpStatusResolver(
                        List.of(new CommonErrorHttpStatusResolver())
                )
        );
    }

    @Test
    void shouldCreateBusinessResponseForWebFlux() {
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_CONFLICT, "版本冲突");

        WebFluxErrorResponse response = responseFactory.from(exception, "trace-same");

        assertEquals(409, response.status());
        assertEquals("COMMON_CONFLICT", response.body().code());
        assertEquals("版本冲突", response.body().message());
        assertEquals("trace-same", response.body().traceId());
    }

    @Test
    void shouldHideUnknownExceptionMessage() {
        WebFluxErrorResponse response = responseFactory.from(
                new IllegalStateException("database password leaked"),
                "trace-internal"
        );

        assertEquals(500, response.status());
        assertEquals("COMMON_INTERNAL_ERROR", response.body().code());
        assertEquals("系统内部错误", response.body().message());
        assertEquals("trace-internal", response.body().traceId());
    }

    @Test
    void shouldMapKnownResponseStatusExceptions() {
        WebFluxErrorResponse badRequest = responseFactory.from(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid raw reason"),
                "trace-400"
        );
        WebFluxErrorResponse notFound = responseFactory.from(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "hidden raw reason"),
                "trace-404"
        );
        WebFluxErrorResponse methodNotAllowed = responseFactory.from(
                new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "hidden raw reason"),
                "trace-405"
        );
        WebFluxErrorResponse unsupportedMediaType = responseFactory.from(
                new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "hidden raw reason"),
                "trace-415"
        );

        assertEquals(400, badRequest.status());
        assertEquals("COMMON_BAD_REQUEST", badRequest.body().code());
        assertEquals("请求参数错误", badRequest.body().message());
        assertEquals("trace-400", badRequest.body().traceId());

        assertEquals(404, notFound.status());
        assertEquals("COMMON_NOT_FOUND", notFound.body().code());
        assertEquals("资源不存在", notFound.body().message());
        assertEquals("trace-404", notFound.body().traceId());

        assertEquals(405, methodNotAllowed.status());
        assertEquals("COMMON_METHOD_NOT_ALLOWED", methodNotAllowed.body().code());
        assertEquals("请求方法不支持", methodNotAllowed.body().message());
        assertEquals("trace-405", methodNotAllowed.body().traceId());

        assertEquals(415, unsupportedMediaType.status());
        assertEquals("COMMON_UNSUPPORTED_MEDIA_TYPE", unsupportedMediaType.body().code());
        assertEquals("媒体类型不支持", unsupportedMediaType.body().message());
        assertEquals("trace-415", unsupportedMediaType.body().traceId());
    }

    @Test
    void shouldMapUnknownResponseStatusToInternalError() {
        WebFluxErrorResponse response = responseFactory.from(
                new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "hidden raw reason"),
                "trace-unknown"
        );

        assertEquals(500, response.status());
        assertEquals("COMMON_INTERNAL_ERROR", response.body().code());
        assertEquals("系统内部错误", response.body().message());
        assertEquals("trace-unknown", response.body().traceId());
    }
}
