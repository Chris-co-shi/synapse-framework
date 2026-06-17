package com.indigo.synapse.webflux.exception;

import com.indigo.synapse.webflux.context.ReactiveRequestContext;
import com.indigo.synapse.webflux.trace.TraceHeaders;
import com.indigo.synapse.webflux.trace.TraceIdGenerator;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * WebFlux 全局异常写出器。
 */
public final class SynapseWebFluxExceptionHandler implements ErrorWebExceptionHandler {

    private final ReactiveWebErrorResponseWriter responseWriter;
    private final WebFluxExceptionResponseFactory responseFactory;

    public SynapseWebFluxExceptionHandler(
            ReactiveWebErrorResponseWriter responseWriter,
            WebFluxExceptionResponseFactory responseFactory) {
        this.responseWriter = Objects.requireNonNull(responseWriter, "responseWriter must not be null");
        this.responseFactory = Objects.requireNonNull(responseFactory, "responseFactory must not be null");
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable throwable) {
        return Mono.deferContextual(contextView -> {
            String traceId = ReactiveRequestContext.traceId(contextView)
                    .orElseGet(() -> fallbackTraceId(exchange));
            WebFluxErrorResponse errorResponse = responseFactory.from(throwable, traceId);
            return responseWriter.write(exchange, errorResponse, traceId);
        });
    }

    private String fallbackTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getResponse().getHeaders().getFirst(TraceHeaders.TRACE_ID);
        return traceId == null || traceId.isBlank() ? TraceIdGenerator.generate() : traceId;
    }

}
