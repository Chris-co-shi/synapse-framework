package com.indigo.synapse.webflux.context;

import reactor.util.context.ContextView;

import java.util.Optional;

/**
 * Reactor Context 读取工具。
 *
 * <p>WebFlux 场景不应依赖 Servlet ThreadLocal。该类型只公开不可信的请求技术上下文，
 * 不承载认证主体、租户或 initiator。认证上下文由 Resource Server 等可信适配器单独建立。</p>
 */
public final class ReactiveRequestContext {

    public static final String TRACE_ID_KEY = "synapse.traceId";
    public static final String REQUEST_ID_KEY = "synapse.requestId";
    public static final String REQUEST_CONTEXT_KEY = "synapse.requestContext";

    private ReactiveRequestContext() {
    }

    public static Optional<String> traceId(ContextView contextView) {
        return get(contextView, TRACE_ID_KEY, String.class);
    }

    public static Optional<String> requestId(ContextView contextView) {
        return get(contextView, REQUEST_ID_KEY, String.class);
    }

    public static Optional<RequestContext> requestContext(ContextView contextView) {
        return get(contextView, REQUEST_CONTEXT_KEY, RequestContext.class);
    }

    private static <T> Optional<T> get(ContextView contextView, String key, Class<T> type) {
        if (contextView == null || !contextView.hasKey(key)) {
            return Optional.empty();
        }
        Object value = contextView.get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }
}
