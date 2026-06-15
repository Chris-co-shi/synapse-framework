package com.indigo.synapse.cloud.context;

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
 * OperationContext 与服务间 HTTP Header 的轻量编解码器。
 *
 * <p>该组件属于 cloud 模块，不复用 MQ codec，也不依赖 WebMVC / WebFlux。它只传播技术上下文；
 * roles、permissions、raw token、password、credential 和业务数据不得写入 Header。</p>
 */
public final class OperationContextHttpHeaderCodec {

    private final Clock clock;

    public OperationContextHttpHeaderCodec() {
        this(Clock.systemUTC());
    }

    public OperationContextHttpHeaderCodec(Clock clock) {
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    /**
     * 将 OperationContext 编码为 HTTP Header Map。
     *
     * @param context 操作上下文
     * @return 编码后的 Header；没有可传播内容时返回空 Map
     */
    public Map<String, String> encode(OperationContext context) {
        if (context == null) {
            return Map.of();
        }
        Map<String, String> headers = new LinkedHashMap<>();
        putIfPresent(headers, SynapseCloudHeaders.CONTEXT_VERSION, SynapseCloudHeaders.CONTEXT_VERSION_VALUE);
        putIfPresent(headers, SynapseCloudHeaders.TRACE_ID, context.traceId());
        putIfPresent(headers, SynapseCloudHeaders.REQUEST_ID, context.requestId());
        putIfPresent(headers, SynapseCloudHeaders.TENANT_ID, context.tenantId());
        putActor(headers, SynapseCloudHeaders.ACTOR_TYPE, SynapseCloudHeaders.ACTOR_ID,
                SynapseCloudHeaders.ACTOR_NAME, context.actor());
        putActor(headers, SynapseCloudHeaders.INITIATOR_TYPE, SynapseCloudHeaders.INITIATOR_ID,
                SynapseCloudHeaders.INITIATOR_NAME, context.initiator());
        putSource(headers, context.source());
        putIfPresent(headers, SynapseCloudHeaders.LOCALE, attribute(context, SynapseCloudHeaders.ATTRIBUTE_LOCALE));
        putIfPresent(headers, SynapseCloudHeaders.TIME_ZONE, attribute(context, SynapseCloudHeaders.ATTRIBUTE_TIME_ZONE));
        return headers;
    }

    /**
     * 将上下文写入目标 Header 容器。
     *
     * @param context 操作上下文
     * @param writer Header 写入端口
     * @param reader Header 读取端口
     * @param overrideExistingHeaders 是否覆盖已有 Header
     */
    public void write(
            OperationContext context,
            HttpHeaderWriter writer,
            HttpHeaderReader reader,
            boolean overrideExistingHeaders
    ) {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null");
        }
        HttpHeaderReader safeReader = reader == null ? name -> false : reader;
        encode(context).forEach((name, value) -> writeIfAllowed(writer, safeReader, overrideExistingHeaders, name, value));
    }

    /**
     * 从 HTTP Header Map 恢复 OperationContext 快照。
     *
     * <p>缺少 actor type 或 actor id 时不伪造 actor，也不默认创建 system actor。</p>
     *
     * @param headers HTTP Header Map
     * @return 可恢复时返回上下文快照
     */
    public Optional<OperationContextSnapshot> decode(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Optional.empty();
        }
        OperationActor actor = readActor(headers, SynapseCloudHeaders.ACTOR_TYPE,
                SynapseCloudHeaders.ACTOR_ID, SynapseCloudHeaders.ACTOR_NAME);
        if (actor == null) {
            return Optional.empty();
        }
        OperationContext context = new OperationContext(
                actor,
                Optional.ofNullable(readActor(headers, SynapseCloudHeaders.INITIATOR_TYPE,
                        SynapseCloudHeaders.INITIATOR_ID, SynapseCloudHeaders.INITIATOR_NAME)).orElse(actor),
                readSource(headers),
                value(headers, SynapseCloudHeaders.TRACE_ID),
                value(headers, SynapseCloudHeaders.TENANT_ID),
                value(headers, SynapseCloudHeaders.REQUEST_ID),
                clock.instant(),
                readAttributes(headers)
        );
        return Optional.of(new OperationContextSnapshot(context));
    }

    private void writeIfAllowed(
            HttpHeaderWriter writer,
            HttpHeaderReader reader,
            boolean overrideExistingHeaders,
            String name,
            String value
    ) {
        if (!overrideExistingHeaders && reader.contains(name)) {
            return;
        }
        writer.write(name, value);
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
        putIfPresent(headers, SynapseCloudHeaders.SOURCE_TYPE, source.type());
        putIfPresent(headers, SynapseCloudHeaders.SOURCE_NAME, source.name());
        putIfPresent(headers, SynapseCloudHeaders.SOURCE_INSTANCE_ID, source.instanceId());
        putIfPresent(headers, SynapseCloudHeaders.SOURCE_ENTRYPOINT, source.entrypoint());
    }

    private OperationActor readActor(Map<String, String> headers, String typeKey, String idKey, String nameKey) {
        OperationActorType type = actorType(value(headers, typeKey));
        String id = value(headers, idKey);
        if (type == null || id == null) {
            return null;
        }
        return new OperationActor(type, id, value(headers, nameKey), value(headers, SynapseCloudHeaders.TENANT_ID), Map.of());
    }

    private OperationSource readSource(Map<String, String> headers) {
        String type = value(headers, SynapseCloudHeaders.SOURCE_TYPE);
        String name = value(headers, SynapseCloudHeaders.SOURCE_NAME);
        String instanceId = value(headers, SynapseCloudHeaders.SOURCE_INSTANCE_ID);
        String entrypoint = value(headers, SynapseCloudHeaders.SOURCE_ENTRYPOINT);
        if (type == null && name == null && instanceId == null && entrypoint == null) {
            return null;
        }
        return new OperationSource(type, name, instanceId, entrypoint, Map.of());
    }

    private Map<String, String> readAttributes(Map<String, String> headers) {
        Map<String, String> attributes = new LinkedHashMap<>();
        putIfPresent(attributes, SynapseCloudHeaders.ATTRIBUTE_LOCALE, value(headers, SynapseCloudHeaders.LOCALE));
        putIfPresent(attributes, SynapseCloudHeaders.ATTRIBUTE_TIME_ZONE, value(headers, SynapseCloudHeaders.TIME_ZONE));
        return attributes;
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

    private String attribute(OperationContext context, String key) {
        return context.attributes() == null ? null : context.attributes().get(key);
    }

    private String value(Map<String, String> headers, String key) {
        String value = headers.get(key);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void putIfPresent(Map<String, String> headers, String key, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        headers.put(key, value.trim());
    }
}
