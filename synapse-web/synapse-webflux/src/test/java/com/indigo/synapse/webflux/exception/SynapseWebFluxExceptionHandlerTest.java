package com.indigo.synapse.webflux.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.web.core.error.CommonErrorHttpStatusResolver;
import com.indigo.synapse.web.core.error.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.web.core.trace.TraceHeaders;
import com.indigo.synapse.webflux.context.ReactiveRequestContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseWebFluxExceptionHandlerTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    private final SynapseWebFluxExceptionHandler handler = new SynapseWebFluxExceptionHandler(
            new ReactiveWebErrorResponseWriter(objectMapper),
            new WebFluxExceptionResponseFactory(new CompositeErrorHttpStatusResolver(
                    List.of(new CommonErrorHttpStatusResolver())
            ))
    );

    @Test
    void shouldWriteUnifiedAuthenticationErrorResponse() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items").build());

        StepVerifier.create(handler.handle(exchange, new SynapseAuthenticationException())
                        .contextWrite(Context.of(ReactiveRequestContext.TRACE_ID_KEY, "trace-1")))
                .verifyComplete();

        assertEquals(401, exchange.getResponse().getRawStatusCode());
        assertEquals("trace-1", exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        StepVerifier.create(exchange.getResponse().getBodyAsString())
                .assertNext(body -> {
                    JsonNode json = read(body);
                    assertEquals(CommonErrorCode.COMMON_UNAUTHORIZED.code(), json.get("code").asText());
                    assertEquals(CommonErrorCode.COMMON_UNAUTHORIZED.message(), json.get("message").asText());
                    assertTrue(json.get("data").isNull());
                    assertEquals("trace-1", json.get("traceId").asText());
                    assertFalse(json.get("timestamp").asText().isBlank());
                })
                .verifyComplete();
    }

    private JsonNode read(String body) {
        try {
            return objectMapper.readTree(body);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
