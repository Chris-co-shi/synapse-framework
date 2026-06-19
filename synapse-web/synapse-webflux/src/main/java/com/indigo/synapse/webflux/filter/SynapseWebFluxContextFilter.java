package com.indigo.synapse.webflux.filter;

import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.webflux.context.OperationContextWebFluxCodec;
import com.indigo.synapse.webflux.context.ReactiveRequestContext;
import com.indigo.synapse.webflux.context.RequestContext;
import com.indigo.synapse.web.core.trace.TraceHeaders;
import com.indigo.synapse.web.core.trace.TraceIdGenerator;
import com.indigo.synapse.web.core.trace.TraceIdResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * WebFlux 请求上下文 Filter。
 *
 * <p>该 Filter 只建立 traceId、requestId 和 OperationContext 的 reactive 技术上下文，不做 Gateway
 * 路由、认证或授权业务。</p>
 */
public final class SynapseWebFluxContextFilter implements WebFilter {

    private final OperationContextWebFluxCodec operationContextCodec;

    public SynapseWebFluxContextFilter(OperationContextWebFluxCodec operationContextCodec) {
        this.operationContextCodec = operationContextCodec;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = TraceIdResolver.resolve(exchange.getRequest().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        String requestId = requestId(exchange);
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = clientIp(exchange);
        RequestContext requestContext = new RequestContext(traceId, requestId, method, path, clientIp);
        Optional<OperationContextSnapshot> snapshot = operationContextCodec.decode(
                exchange.getRequest().getHeaders(),
                traceId,
                requestId,
                method,
                path
        );

        exchange.getResponse().getHeaders().set(TraceHeaders.TRACE_ID, traceId);
        exchange.getResponse().getHeaders().set(TraceHeaders.REQUEST_ID, requestId);

        return chain.filter(exchange).contextWrite(context -> withContext(context, requestContext, snapshot));
    }

    private static Context withContext(
            Context context,
            RequestContext requestContext,
            Optional<OperationContextSnapshot> snapshot
    ) {
        Context next = context
                .put(ReactiveRequestContext.TRACE_ID_KEY, requestContext.traceId())
                .put(ReactiveRequestContext.REQUEST_ID_KEY, requestContext.requestId())
                .put(ReactiveRequestContext.REQUEST_CONTEXT_KEY, requestContext);
        return snapshot.map(operationContextSnapshot -> next.put(ReactiveRequestContext.OPERATION_CONTEXT_SNAPSHOT_KEY, operationContextSnapshot)).orElse(next);
    }

    private static String requestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(TraceHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank() || !TraceIdResolver.isValid(requestId.trim())) {
            return TraceIdGenerator.generate();
        }
        return requestId.trim();
    }

    private static String clientIp(ServerWebExchange exchange) {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        return address == null || address.getAddress() == null ? "" : address.getAddress().getHostAddress();
    }
}
