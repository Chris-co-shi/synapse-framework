package com.indigo.synapse.webflux.filter;

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
/**
 * WebFlux 请求上下文 Filter。
 *
 * <p>该 Filter 只建立 traceId、requestId 和当前传输入口信息。普通 HTTP Header 不得建立 actor、tenant
 * 或 initiator；认证主体只能由完成 Token 验证的安全适配器建立。</p>
 */
public final class SynapseWebFluxContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = TraceIdResolver.resolve(exchange.getRequest().getHeaders().getFirst(TraceHeaders.TRACE_ID));
        String requestId = requestId(exchange);
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String clientIp = clientIp(exchange);
        RequestContext requestContext = new RequestContext(traceId, requestId, method, path, clientIp);
        exchange.getResponse().getHeaders().set(TraceHeaders.TRACE_ID, traceId);
        exchange.getResponse().getHeaders().set(TraceHeaders.REQUEST_ID, requestId);

        return chain.filter(exchange).contextWrite(context -> withContext(context, requestContext));
    }

    private static Context withContext(
            Context context,
            RequestContext requestContext
    ) {
        return context
                .put(ReactiveRequestContext.TRACE_ID_KEY, requestContext.traceId())
                .put(ReactiveRequestContext.REQUEST_ID_KEY, requestContext.requestId())
                .put(ReactiveRequestContext.REQUEST_CONTEXT_KEY, requestContext);
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
