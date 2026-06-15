package com.indigo.synapse.webflux.context;

import com.indigo.synapse.core.context.OperationContextSnapshot;
import reactor.util.context.ContextView;

import java.util.Optional;

/**
 * Reactor Context 读取工具。
 *
 * <p>WebFlux 场景不应依赖 Servlet ThreadLocal。需要读取 traceId、requestId 或 OperationContext 时，
 * 应从 Reactor Context 中显式获取。</p>
 */
public final class ReactiveRequestContext {

    public static final String TRACE_ID_KEY = "synapse.traceId";
    public static final String REQUEST_ID_KEY = "synapse.requestId";
    public static final String REQUEST_CONTEXT_KEY = "synapse.requestContext";
    public static final String OPERATION_CONTEXT_SNAPSHOT_KEY = "synapse.operationContextSnapshot";

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

    public static Optional<OperationContextSnapshot> operationContextSnapshot(ContextView contextView) {
        return get(contextView, OPERATION_CONTEXT_SNAPSHOT_KEY, OperationContextSnapshot.class);
    }

    private static <T> Optional<T> get(ContextView contextView, String key, Class<T> type) {
        if (contextView == null || !contextView.hasKey(key)) {
            return Optional.empty();
        }
        Object value = contextView.get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }
}
