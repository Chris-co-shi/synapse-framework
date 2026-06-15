package com.indigo.synapse.webflux.context;

import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationContextSnapshotCarrier;
import com.indigo.synapse.core.context.OperationContextSnapshotCodec;
import org.springframework.http.HttpHeaders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ServerWebExchange Header 到 OperationContext 的轻量解码器。
 *
 * <p>该解码器只恢复技术上下文，不做 Header 签名校验、认证、授权或 Gateway 业务判定。可信 Header
 * 的注入和校验应由 Platform Gateway 或后续安全适配完成。</p>
 */
public final class OperationContextWebFluxCodec {

    private final OperationContextSnapshotCodec codec;

    public OperationContextWebFluxCodec() {
        this(new OperationContextSnapshotCodec());
    }

    public OperationContextWebFluxCodec(OperationContextSnapshotCodec codec) {
        this.codec = codec == null ? new OperationContextSnapshotCodec() : codec;
    }

    public Optional<OperationContextSnapshot> decode(
            HttpHeaders headers,
            String traceId,
            String requestId,
            String method,
            String path
    ) {
        if (headers == null) {
            return Optional.empty();
        }
        Map<String, String> values = new LinkedHashMap<>();
        put(values, OperationContextPropagationKeys.TRACE_ID, traceId);
        put(values, OperationContextPropagationKeys.REQUEST_ID, requestId);
        put(values, OperationContextPropagationKeys.TENANT_ID, headers.getFirst(OperationContextHeaders.TENANT_ID));
        put(values, OperationContextPropagationKeys.ACTOR_TYPE, headers.getFirst(OperationContextHeaders.ACTOR_TYPE));
        put(values, OperationContextPropagationKeys.ACTOR_ID, headers.getFirst(OperationContextHeaders.ACTOR_ID));
        put(values, OperationContextPropagationKeys.ACTOR_NAME, headers.getFirst(OperationContextHeaders.ACTOR_NAME));
        put(values, OperationContextPropagationKeys.INITIATOR_TYPE,
                headers.getFirst(OperationContextHeaders.INITIATOR_TYPE));
        put(values, OperationContextPropagationKeys.INITIATOR_ID,
                headers.getFirst(OperationContextHeaders.INITIATOR_ID));
        put(values, OperationContextPropagationKeys.INITIATOR_NAME,
                headers.getFirst(OperationContextHeaders.INITIATOR_NAME));
        put(values, OperationContextPropagationKeys.SOURCE_TYPE,
                fallback(headers.getFirst(OperationContextHeaders.SOURCE_TYPE), "HTTP"));
        put(values, OperationContextPropagationKeys.SOURCE_NAME,
                fallback(headers.getFirst(OperationContextHeaders.SOURCE_NAME), "webflux"));
        put(values, OperationContextPropagationKeys.SOURCE_INSTANCE_ID,
                headers.getFirst(OperationContextHeaders.SOURCE_INSTANCE_ID));
        put(values, OperationContextPropagationKeys.SOURCE_ENTRYPOINT,
                fallback(headers.getFirst(OperationContextHeaders.SOURCE_ENTRYPOINT), method + " " + path));
        return codec.decode(new OperationContextSnapshotCarrier(values));
    }

    private static String fallback(String value, String fallback) {
        String trimmed = trim(value);
        return trimmed == null ? fallback : trimmed;
    }

    private static void put(Map<String, String> values, String key, String value) {
        String trimmed = trim(value);
        if (trimmed == null) {
            return;
        }
        values.put(key, trimmed);
    }

    private static String trim(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
