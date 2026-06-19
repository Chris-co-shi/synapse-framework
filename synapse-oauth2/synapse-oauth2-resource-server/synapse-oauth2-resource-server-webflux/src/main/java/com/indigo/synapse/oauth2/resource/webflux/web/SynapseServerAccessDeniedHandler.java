package com.indigo.synapse.oauth2.resource.webflux.web;

import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import com.indigo.synapse.web.core.trace.TraceIdGenerator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive Resource Server 403 写出器。
 */
public final class SynapseServerAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final WebFluxExceptionResponseFactory responseFactory;
    private final ReactiveWebErrorResponseWriter responseWriter;

    public SynapseServerAccessDeniedHandler(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        String traceId = TraceIdGenerator.generate();
        return responseWriter.write(exchange, responseFactory.from(new SynapseAccessDeniedException(), traceId), traceId);
    }
}
