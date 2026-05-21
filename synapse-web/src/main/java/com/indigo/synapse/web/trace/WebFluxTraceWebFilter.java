package com.indigo.synapse.web.trace;

import com.indigo.synapse.web.context.RequestContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public final class WebFluxTraceWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        RequestContext requestContext = WebTraceLifecycle.start(
                request.getHeaders().getFirst(TraceHeaders.TRACE_ID),
                request.getMethod().name(),
                request.getPath().value(),
                remoteAddress(request)
        );
        exchange.getResponse().getHeaders().set(TraceHeaders.TRACE_ID, requestContext.traceId());
        return chain.filter(exchange)
                .doFinally(signalType -> WebTraceLifecycle.end());
    }

    private static String remoteAddress(ServerHttpRequest request) {
        if (request.getRemoteAddress() == null || request.getRemoteAddress().getAddress() == null) {
            return null;
        }
        return request.getRemoteAddress().getAddress().getHostAddress();
    }
}
