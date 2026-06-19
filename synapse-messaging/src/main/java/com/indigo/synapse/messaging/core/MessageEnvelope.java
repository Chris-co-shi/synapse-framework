package com.indigo.synapse.messaging.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 通用 MQ 消息外壳。
 *
 * <p>框架只定义传输外壳和技术元数据，payload、topic、tag、key 均由消费方定义。
 * 该类型不表达业务通知、站内信或模板消息含义，也不承诺可靠投递；可靠投递应由未来 MQ 适配器
 * 或消费方基础设施负责。</p>
 */
public record MessageEnvelope(
        String messageId,
        String messageType,
        String topic,
        String tag,
        String key,
        String idempotentKey,
        String sourceService,
        String contentType,
        String schemaVersion,
        Map<String, String> headers,
        String payload,
        String traceId,
        String tenantId,
        Instant occurredAt,
        Instant createdAt
) {

    public MessageEnvelope {
        validate(messageId, "messageId");
        validate(messageType, "messageType");
        validate(topic, "topic");
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * 创建通用 MQ 消息外壳，并由框架生成 messageId 与 createdAt。
     */
    public static MessageEnvelope create(
            String messageType,
            String topic,
            String tag,
            String key,
            String idempotentKey,
            String sourceService,
            String contentType,
            String schemaVersion,
            Map<String, String> headers,
            String payload,
            String traceId,
            String tenantId,
            Instant occurredAt
    ) {
        return new MessageEnvelope(
                UUID.randomUUID().toString(),
                messageType,
                topic,
                tag,
                key,
                idempotentKey,
                sourceService,
                contentType,
                schemaVersion,
                headers,
                payload,
                traceId,
                tenantId,
                occurredAt,
                Instant.now()
        );
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
