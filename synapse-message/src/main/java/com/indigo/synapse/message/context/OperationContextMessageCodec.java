package com.indigo.synapse.message.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationSource;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OperationContext 与消息 header 之间的轻量编解码器。
 *
 * <p>该组件只传播跨模块技术上下文，不传播角色、权限和业务字段。</p>
 */
public final class OperationContextMessageCodec {

    private final Clock clock;

    public OperationContextMessageCodec() {
        this(Clock.systemUTC());
    }

    public OperationContextMessageCodec(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public Map<String, String> encode(OperationContextSnapshot snapshot) {
        if (snapshot == null || snapshot.context() == null) {
            return Map.of();
        }
        OperationContext context = snapshot.context();
        Map<String, String> headers = new LinkedHashMap<>();
        putIfPresent(headers, MessageContextHeaders.TRACE_ID, context.traceId());
        putIfPresent(headers, MessageContextHeaders.REQUEST_ID, context.requestId());
        putIfPresent(headers, MessageContextHeaders.TENANT_ID, context.tenantId());
        putActor(headers, MessageContextHeaders.ACTOR_TYPE, MessageContextHeaders.ACTOR_ID,
                MessageContextHeaders.ACTOR_NAME, context.actor());
        putActor(headers, MessageContextHeaders.INITIATOR_TYPE, MessageContextHeaders.INITIATOR_ID,
                MessageContextHeaders.INITIATOR_NAME, context.initiator());
        putSource(headers, context.source());
        return headers;
    }

    public Optional<OperationContextSnapshot> decode(Map<String, String> headers) {
        if (headers == null || headers.isEmpty() || !containsContextHeader(headers)) {
            return Optional.empty();
        }
        OperationContext context = new OperationContext(
                readActor(headers, MessageContextHeaders.ACTOR_TYPE, MessageContextHeaders.ACTOR_ID,
                        MessageContextHeaders.ACTOR_NAME),
                readActor(headers, MessageContextHeaders.INITIATOR_TYPE, MessageContextHeaders.INITIATOR_ID,
                        MessageContextHeaders.INITIATOR_NAME),
                readSource(headers),
                value(headers, MessageContextHeaders.TRACE_ID),
                value(headers, MessageContextHeaders.TENANT_ID),
                value(headers, MessageContextHeaders.REQUEST_ID),
                clock.instant(),
                Map.of()
        );
        return Optional.of(new OperationContextSnapshot(context));
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

    private void putActor(Map<String, String> headers, String typeKey, String idKey, String nameKey, OperationActor actor) {
        if (actor == null) {
            return;
        }
        putIfPresent(headers, typeKey, actor.type() == null ? null : actor.type().name());
        putIfPresent(headers, idKey, actor.id());
        putIfPresent(headers, nameKey, actor.name());
    }

    private void putSource(Map<String, String> headers, OperationSource source) {
        if (source == null) {
            return;
        }
        putIfPresent(headers, MessageContextHeaders.SOURCE_TYPE, source.type());
        putIfPresent(headers, MessageContextHeaders.SOURCE_NAME, source.name());
        putIfPresent(headers, MessageContextHeaders.SOURCE_INSTANCE_ID, source.instanceId());
        putIfPresent(headers, MessageContextHeaders.SOURCE_ENTRYPOINT, source.entrypoint());
    }

    private OperationActor readActor(Map<String, String> headers, String typeKey, String idKey, String nameKey) {
        String type = value(headers, typeKey);
        String id = value(headers, idKey);
        String name = value(headers, nameKey);
        if (type == null && id == null && name == null) {
            return null;
        }
        OperationActorType actorType = type == null ? null : OperationActorType.valueOf(type);
        if (actorType == null) {
            return null;
        }
        return new OperationActor(actorType, id, name, value(headers, MessageContextHeaders.TENANT_ID), Map.of());
    }

    private OperationSource readSource(Map<String, String> headers) {
        String type = value(headers, MessageContextHeaders.SOURCE_TYPE);
        String name = value(headers, MessageContextHeaders.SOURCE_NAME);
        String instanceId = value(headers, MessageContextHeaders.SOURCE_INSTANCE_ID);
        String entrypoint = value(headers, MessageContextHeaders.SOURCE_ENTRYPOINT);
        if (type == null && name == null && instanceId == null && entrypoint == null) {
            return null;
        }
        return new OperationSource(type, name, instanceId, entrypoint, Map.of());
    }

    private boolean hasValue(Map<String, String> headers, String key) {
        return value(headers, key) != null;
    }

    private String value(Map<String, String> headers, String key) {
        String value = headers.get(key);
        return value == null || value.isBlank() ? null : value;
    }

    private void putIfPresent(Map<String, String> headers, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        headers.put(key, value);
    }
}
