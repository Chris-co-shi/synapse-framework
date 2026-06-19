package com.indigo.synapse.oauth2.resource.webflux.web;

import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import com.indigo.synapse.webflux.trace.TraceIdGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Reactive Resource Server 401 写出器。
 */
public final class SynapseServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final WebFluxExceptionResponseFactory responseFactory;
    private final ReactiveWebErrorResponseWriter responseWriter;

    public SynapseServerAuthenticationEntryPoint(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter) {
        this.responseFactory = responseFactory;
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        String traceId = TraceIdGenerator.generate();
        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return responseWriter.write(exchange, responseFactory.from(new SynapseAuthenticationException(
                OAuth2ErrorCode.OAUTH2_INVALID_TOKEN,
                ex == null ? OAuth2ErrorCode.OAUTH2_INVALID_TOKEN.message() : ex.getMessage()
        ), traceId), traceId);
    }
}
