package com.indigo.synapse.mq.context;

import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationContextSnapshotCarrier;
import com.indigo.synapse.core.context.OperationContextSnapshotCodec;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OperationContext 与消息 header 之间的轻量编解码器。
 *
 * <p>该组件只传播跨模块技术上下文，不传播角色、权限和业务字段。消息 header 名称保持 MQ
 * 模块的小写契约，内部通过 core carrier 统一 OperationContext 编解码规则。</p>
 */
public final class OperationContextMessageCodec {

    private final OperationContextSnapshotCodec codec;

    public OperationContextMessageCodec() {
        this(Clock.systemUTC());
    }

    public OperationContextMessageCodec(Clock clock) {
        this.codec = new OperationContextSnapshotCodec(clock);
    }

    public Map<String, String> encode(OperationContextSnapshot snapshot) {
        Map<String, String> coreValues = codec.encode(snapshot).values();
        if (coreValues.isEmpty()) {
            return Map.of();
        }
        Map<String, String> headers = new LinkedHashMap<>();
        copy(headers, MessageContextHeaders.TRACE_ID, coreValues, OperationContextPropagationKeys.TRACE_ID);
        copy(headers, MessageContextHeaders.REQUEST_ID, coreValues, OperationContextPropagationKeys.REQUEST_ID);
        copy(headers, MessageContextHeaders.TENANT_ID, coreValues, OperationContextPropagationKeys.TENANT_ID);
        copy(headers, MessageContextHeaders.ACTOR_TYPE, coreValues, OperationContextPropagationKeys.ACTOR_TYPE);
        copy(headers, MessageContextHeaders.ACTOR_ID, coreValues, OperationContextPropagationKeys.ACTOR_ID);
        copy(headers, MessageContextHeaders.ACTOR_NAME, coreValues, OperationContextPropagationKeys.ACTOR_NAME);
        copy(headers, MessageContextHeaders.INITIATOR_TYPE, coreValues, OperationContextPropagationKeys.INITIATOR_TYPE);
        copy(headers, MessageContextHeaders.INITIATOR_ID, coreValues, OperationContextPropagationKeys.INITIATOR_ID);
        copy(headers, MessageContextHeaders.INITIATOR_NAME, coreValues, OperationContextPropagationKeys.INITIATOR_NAME);
        copy(headers, MessageContextHeaders.SOURCE_TYPE, coreValues, OperationContextPropagationKeys.SOURCE_TYPE);
        copy(headers, MessageContextHeaders.SOURCE_NAME, coreValues, OperationContextPropagationKeys.SOURCE_NAME);
        copy(headers, MessageContextHeaders.SOURCE_INSTANCE_ID,
                coreValues, OperationContextPropagationKeys.SOURCE_INSTANCE_ID);
        copy(headers, MessageContextHeaders.SOURCE_ENTRYPOINT,
                coreValues, OperationContextPropagationKeys.SOURCE_ENTRYPOINT);
        return headers;
    }

    public Optional<OperationContextSnapshot> decode(Map<String, String> headers) {
        if (headers == null || headers.isEmpty() || !containsContextHeader(headers)) {
            return Optional.empty();
        }
        return codec.decode(new OperationContextSnapshotCarrier(toCoreValues(headers)));
    }

    private Map<String, String> toCoreValues(Map<String, String> headers) {
        Map<String, String> values = new LinkedHashMap<>();
        copy(values, OperationContextPropagationKeys.TRACE_ID, headers, MessageContextHeaders.TRACE_ID);
        copy(values, OperationContextPropagationKeys.REQUEST_ID, headers, MessageContextHeaders.REQUEST_ID);
        copy(values, OperationContextPropagationKeys.TENANT_ID, headers, MessageContextHeaders.TENANT_ID);
        copy(values, OperationContextPropagationKeys.ACTOR_TYPE, headers, MessageContextHeaders.ACTOR_TYPE);
        copy(values, OperationContextPropagationKeys.ACTOR_ID, headers, MessageContextHeaders.ACTOR_ID);
        copy(values, OperationContextPropagationKeys.ACTOR_NAME, headers, MessageContextHeaders.ACTOR_NAME);
        copy(values, OperationContextPropagationKeys.INITIATOR_TYPE, headers, MessageContextHeaders.INITIATOR_TYPE);
        copy(values, OperationContextPropagationKeys.INITIATOR_ID, headers, MessageContextHeaders.INITIATOR_ID);
        copy(values, OperationContextPropagationKeys.INITIATOR_NAME, headers, MessageContextHeaders.INITIATOR_NAME);
        copy(values, OperationContextPropagationKeys.SOURCE_TYPE, headers, MessageContextHeaders.SOURCE_TYPE);
        copy(values, OperationContextPropagationKeys.SOURCE_NAME, headers, MessageContextHeaders.SOURCE_NAME);
        copy(values, OperationContextPropagationKeys.SOURCE_INSTANCE_ID,
                headers, MessageContextHeaders.SOURCE_INSTANCE_ID);
        copy(values, OperationContextPropagationKeys.SOURCE_ENTRYPOINT,
                headers, MessageContextHeaders.SOURCE_ENTRYPOINT);
        return values;
    }

    private boolean containsContextHeader(Map<String, String> headers) {
        return hasValue(headers, MessageContextHeaders.TRACE_ID)
                || hasValue(headers, MessageContextHeaders.REQUEST_ID)
                || hasValue(headers, MessageContextHeaders.TENANT_ID)
                || hasValue(headers, MessageContextHeaders.ACTOR_TYPE)
                || hasValue(headers, MessageContextHeaders.ACTOR_ID)
                || hasValue(headers, MessageContextHeaders.ACTOR_NAME)
                || hasValue(headers, MessageContextHeaders.INITIATOR_TYPE)
                || hasValue(headers, MessageContextHeaders.INITIATOR_ID)
                || hasValue(headers, MessageContextHeaders.INITIATOR_NAME)
                || hasValue(headers, MessageContextHeaders.SOURCE_TYPE)
                || hasValue(headers, MessageContextHeaders.SOURCE_NAME)
                || hasValue(headers, MessageContextHeaders.SOURCE_INSTANCE_ID)
                || hasValue(headers, MessageContextHeaders.SOURCE_ENTRYPOINT);
    }

    private void copy(Map<String, String> target, String targetKey, Map<String, String> source, String sourceKey) {
        String value = value(source, sourceKey);
        if (value == null) {
            return;
        }
        target.put(targetKey, value);
    }

    private boolean hasValue(Map<String, String> headers, String key) {
        return value(headers, key) != null;
    }

    private String value(Map<String, String> headers, String key) {
        String value = headers.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
