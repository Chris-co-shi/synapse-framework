package com.indigo.synapse.cloud.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationContextSnapshotCarrier;
import com.indigo.synapse.core.context.OperationContextSnapshotCodec;

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

    private final OperationContextSnapshotCodec codec;

    public OperationContextHttpHeaderCodec() {
        this(Clock.systemUTC());
    }

    public OperationContextHttpHeaderCodec(Clock clock) {
        this.codec = new OperationContextSnapshotCodec(clock);
    }

    /**
     * 将 OperationContext 编码为 HTTP Header Map。
     *
     * @param context 操作上下文
     * @return 编码后的 Header；没有可传播内容时返回空 Map
     */
    public Map<String, String> encode(OperationContext context) {
        Map<String, String> headers = new LinkedHashMap<>(
                codec.encode(new OperationContextSnapshot(context)).values()
        );
        if (!headers.isEmpty()) {
            putIfPresent(headers, SynapseCloudHeaders.CONTEXT_VERSION, SynapseCloudHeaders.CONTEXT_VERSION_VALUE);
        }
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
        return codec.decode(new OperationContextSnapshotCarrier(headers))
                .map(snapshot -> withCloudAttributes(snapshot, headers));
    }

    private OperationContextSnapshot withCloudAttributes(OperationContextSnapshot snapshot, Map<String, String> headers) {
        Map<String, String> attributes = new LinkedHashMap<>(snapshot.context().attributes());
        putIfPresent(attributes, SynapseCloudHeaders.ATTRIBUTE_LOCALE, value(headers, SynapseCloudHeaders.LOCALE));
        putIfPresent(attributes, SynapseCloudHeaders.ATTRIBUTE_TIME_ZONE, value(headers, SynapseCloudHeaders.TIME_ZONE));
        if (attributes.equals(snapshot.context().attributes())) {
            return snapshot;
        }
        OperationContext context = snapshot.context();
        return new OperationContextSnapshot(new OperationContext(
                context.actor(),
                context.initiator(),
                context.source(),
                context.traceId(),
                context.tenantId(),
                context.requestId(),
                context.occurredAt(),
                attributes
        ));
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

    private String attribute(OperationContext context, String key) {
        return context == null || context.attributes() == null ? null : context.attributes().get(key);
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
