package com.indigo.synapse.webflux.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.webflux.trace.TraceHeaders;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * WebFlux 统一错误响应写出器。
 *
 * <p>该组件不依赖 Security/OAuth2/Gateway，可被 reactive Resource Server 复用。</p>
 */
public final class ReactiveWebErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ReactiveWebErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
    }

    public Mono<Void> write(ServerWebExchange exchange, WebFluxErrorResponse errorResponse, String traceId) {
        exchange.getResponse().setRawStatusCode(errorResponse.status());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.name());
        exchange.getResponse().getHeaders().set(TraceHeaders.TRACE_ID, traceId);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(write(errorResponse));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private byte[] write(WebFluxErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse.body());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("failed to write webflux error response", exception);
        }
    }
}
