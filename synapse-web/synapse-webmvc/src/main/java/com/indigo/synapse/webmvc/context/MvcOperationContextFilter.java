package com.indigo.synapse.webmvc.context;

import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.OperationContextSnapshotCarrier;
import com.indigo.synapse.core.context.OperationContextSnapshotCodec;
import com.indigo.synapse.webmvc.trace.TraceHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet MVC OperationContext 恢复 Filter。
 *
 * <p>该 Filter 只根据标准上下文 Header 恢复技术上下文，不做认证、授权或 Gateway 判定。
 * 缺少 actor type 或 actor id 时不会伪造 actor，也不会默认创建 system actor。</p>
 */
public final class MvcOperationContextFilter extends OncePerRequestFilter {

    private final OperationContextSnapshotCodec codec;

    public MvcOperationContextFilter() {
        this(new OperationContextSnapshotCodec());
    }

    public MvcOperationContextFilter(OperationContextSnapshotCodec codec) {
        this.codec = codec == null ? new OperationContextSnapshotCodec() : codec;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional<OperationContextScope> scope = codec.decode(new OperationContextSnapshotCarrier(headers(request)))
                .map(OperationContextHolder::restore);
        try {
            filterChain.doFilter(request, response);
        } finally {
            scope.ifPresent(OperationContextScope::close);
        }
    }

    private Map<String, String> headers(HttpServletRequest request) {
        Map<String, String> values = new LinkedHashMap<>();
        put(values, OperationContextPropagationKeys.TRACE_ID, request.getHeader(TraceHeaders.TRACE_ID));
        put(values, OperationContextPropagationKeys.REQUEST_ID,
                request.getHeader(OperationContextPropagationKeys.REQUEST_ID));
        put(values, OperationContextPropagationKeys.TENANT_ID,
                request.getHeader(OperationContextPropagationKeys.TENANT_ID));
        put(values, OperationContextPropagationKeys.ACTOR_TYPE,
                request.getHeader(OperationContextPropagationKeys.ACTOR_TYPE));
        put(values, OperationContextPropagationKeys.ACTOR_ID,
                request.getHeader(OperationContextPropagationKeys.ACTOR_ID));
        put(values, OperationContextPropagationKeys.ACTOR_NAME,
                request.getHeader(OperationContextPropagationKeys.ACTOR_NAME));
        put(values, OperationContextPropagationKeys.INITIATOR_TYPE,
                request.getHeader(OperationContextPropagationKeys.INITIATOR_TYPE));
        put(values, OperationContextPropagationKeys.INITIATOR_ID,
                request.getHeader(OperationContextPropagationKeys.INITIATOR_ID));
        put(values, OperationContextPropagationKeys.INITIATOR_NAME,
                request.getHeader(OperationContextPropagationKeys.INITIATOR_NAME));
        put(values, OperationContextPropagationKeys.SOURCE_TYPE,
                request.getHeader(OperationContextPropagationKeys.SOURCE_TYPE));
        put(values, OperationContextPropagationKeys.SOURCE_NAME,
                request.getHeader(OperationContextPropagationKeys.SOURCE_NAME));
        put(values, OperationContextPropagationKeys.SOURCE_INSTANCE_ID,
                request.getHeader(OperationContextPropagationKeys.SOURCE_INSTANCE_ID));
        put(values, OperationContextPropagationKeys.SOURCE_ENTRYPOINT,
                request.getHeader(OperationContextPropagationKeys.SOURCE_ENTRYPOINT));
        return values;
    }

    private void put(Map<String, String> values, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        values.put(key, value.trim());
    }
}
