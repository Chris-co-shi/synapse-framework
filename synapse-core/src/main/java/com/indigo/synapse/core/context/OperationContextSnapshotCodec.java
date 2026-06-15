package com.indigo.synapse.core.context;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OperationContextSnapshot 与纯字符串 Carrier 的编解码器。
 *
 * <p>该类型只处理纯 Java Map，不依赖任何具体协议。缺少 actor type 或 actor id 时不会伪造 system actor。</p>
 */
public final class OperationContextSnapshotCodec {

    private final Clock clock;

    public OperationContextSnapshotCodec() {
        this(Clock.systemUTC());
    }

    public OperationContextSnapshotCodec(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public OperationContextSnapshotCarrier encode(OperationContextSnapshot snapshot) {
        if (snapshot == null || snapshot.context() == null) {
            return new OperationContextSnapshotCarrier(Map.of());
        }
        OperationContext context = snapshot.context();
        Map<String, String> values = new LinkedHashMap<>();
        putIfPresent(values, OperationContextPropagationKeys.TRACE_ID, context.traceId());
        putIfPresent(values, OperationContextPropagationKeys.REQUEST_ID, context.requestId());
        putIfPresent(values, OperationContextPropagationKeys.TENANT_ID, context.tenantId());
        putActor(values, OperationContextPropagationKeys.ACTOR_TYPE, OperationContextPropagationKeys.ACTOR_ID,
                OperationContextPropagationKeys.ACTOR_NAME, context.actor());
        putActor(values, OperationContextPropagationKeys.INITIATOR_TYPE, OperationContextPropagationKeys.INITIATOR_ID,
                OperationContextPropagationKeys.INITIATOR_NAME, context.initiator());
        putSource(values, context.source());
        return new OperationContextSnapshotCarrier(values);
    }

    public Optional<OperationContextSnapshot> decode(OperationContextSnapshotCarrier carrier) {
        if (carrier == null || carrier.isEmpty()) {
            return Optional.empty();
        }
        Map<String, String> values = carrier.values();
        OperationActor actor = readActor(values, OperationContextPropagationKeys.ACTOR_TYPE,
                OperationContextPropagationKeys.ACTOR_ID, OperationContextPropagationKeys.ACTOR_NAME);
        if (actor == null) {
            return Optional.empty();
        }
        OperationActor initiator = readActor(values, OperationContextPropagationKeys.INITIATOR_TYPE,
                OperationContextPropagationKeys.INITIATOR_ID, OperationContextPropagationKeys.INITIATOR_NAME);
        OperationContext context = new OperationContext(
                actor,
                initiator == null ? actor : initiator,
                readSource(values),
                value(values, OperationContextPropagationKeys.TRACE_ID),
                value(values, OperationContextPropagationKeys.TENANT_ID),
                value(values, OperationContextPropagationKeys.REQUEST_ID),
                clock.instant(),
                Map.of()
        );
        return Optional.of(new OperationContextSnapshot(context));
    }

    private void putActor(Map<String, String> values, String typeKey, String idKey, String nameKey, OperationActor actor) {
        if (actor == null) {
            return;
        }
        putIfPresent(values, typeKey, actor.type() == null ? null : actor.type().name());
        putIfPresent(values, idKey, actor.id());
        putIfPresent(values, nameKey, actor.name());
    }

    private void putSource(Map<String, String> values, OperationSource source) {
        if (source == null) {
            return;
        }
        putIfPresent(values, OperationContextPropagationKeys.SOURCE_TYPE, source.type());
        putIfPresent(values, OperationContextPropagationKeys.SOURCE_NAME, source.name());
        putIfPresent(values, OperationContextPropagationKeys.SOURCE_INSTANCE_ID, source.instanceId());
        putIfPresent(values, OperationContextPropagationKeys.SOURCE_ENTRYPOINT, source.entrypoint());
    }

    private OperationActor readActor(Map<String, String> values, String typeKey, String idKey, String nameKey) {
        OperationActorType type = actorType(value(values, typeKey));
        String id = value(values, idKey);
        if (type == null || id == null) {
            return null;
        }
        return new OperationActor(type, id, value(values, nameKey), value(values, OperationContextPropagationKeys.TENANT_ID), Map.of());
    }

    private OperationSource readSource(Map<String, String> values) {
        String type = value(values, OperationContextPropagationKeys.SOURCE_TYPE);
        String name = value(values, OperationContextPropagationKeys.SOURCE_NAME);
        String instanceId = value(values, OperationContextPropagationKeys.SOURCE_INSTANCE_ID);
        String entrypoint = value(values, OperationContextPropagationKeys.SOURCE_ENTRYPOINT);
        if (type == null && name == null && instanceId == null && entrypoint == null) {
            return null;
        }
        return new OperationSource(type, name, instanceId, entrypoint, Map.of());
    }

    private OperationActorType actorType(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OperationActorType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String value(Map<String, String> values, String key) {
        String value = values.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void putIfPresent(Map<String, String> values, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        values.put(key, value.trim());
    }
}
