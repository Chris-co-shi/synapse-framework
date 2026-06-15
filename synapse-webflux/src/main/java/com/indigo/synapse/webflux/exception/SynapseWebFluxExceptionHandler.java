package com.indigo.synapse.webflux.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.webflux.context.ReactiveRequestContext;
import com.indigo.synapse.webflux.trace.TraceHeaders;
import com.indigo.synapse.webflux.trace.TraceIdGenerator;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * WebFlux 全局异常写出器。
 */
public final class SynapseWebFluxExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final WebFluxExceptionResponseFactory responseFactory;

    public SynapseWebFluxExceptionHandler(ObjectMapper objectMapper, WebFluxExceptionResponseFactory responseFactory) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory must not be null");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        return Mono.deferContextual(contextView -> {
            String traceId = ReactiveRequestContext.traceId(contextView)
                    .orElseGet(() -> fallbackTraceId(exchange));
            WebFluxErrorResponse errorResponse = responseFactory.from(throwable, traceId);
            exchange.getResponse().setRawStatusCode(errorResponse.status());
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.name());
            exchange.getResponse().getHeaders().set(TraceHeaders.TRACE_ID, traceId);
            byte[] payload = write(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(payload);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        });
    }

    private String fallbackTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID);
        return traceId == null || traceId.isBlank() ? TraceIdGenerator.generate() : traceId;
    }

    private byte[] write(WebFluxErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse.body());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to write webflux error response", exception);
        }
    }
}
