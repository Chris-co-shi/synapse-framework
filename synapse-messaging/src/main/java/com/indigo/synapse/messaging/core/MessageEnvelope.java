package com.indigo.synapse.messaging.core;

import java.util.Objects;

/**
 * Broker 中立的消息传输外壳。
 *
 * <p>Envelope 只组合稳定元数据、逻辑目的地和负载，不暴露 Kafka、RocketMQ 等
 * Broker 的原生消息类型。负载序列化格式由 {@link MessageMetadata#contentType()} 描述。</p>
 *
 * @param metadata 消息身份和技术元数据
 * @param destination 逻辑目的地
 * @param payload 已序列化负载
 */
public record MessageEnvelope(MessageMetadata metadata, MessageDestination destination, String payload) {

    public MessageEnvelope {
        metadata = Objects.requireNonNull(metadata, "metadata must not be null");
        destination = Objects.requireNonNull(destination, "destination must not be null");
        payload = Objects.requireNonNull(payload, "payload must not be null");
    }

    /** 返回用于消费幂等的稳定标识，领域事件优先使用 eventId。 */
    public String idempotencyKey() {
        return metadata.eventId() == null ? metadata.messageId() : metadata.eventId();
    }
}
