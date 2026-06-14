package com.indigo.synapse.mq.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 通用消息外壳。
 *
 * <p>框架只定义传输外壳和技术元数据，payload、topic、tag、key 均由消费方定义。
 * 该类型不表达业务通知含义，也不承诺可靠投递。</p>
 */
public record MessageEnvelope(
        String messageId,
        String topic,
        String tag,
        String key,
        Map<String, String> headers,
        String payload,
        String traceId,
        String tenantId,
        Instant createdAt
) {

    public MessageEnvelope {
        validate(messageId, "messageId");
        validate(topic, "topic");
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    /**
     * 创建通用消息外壳。
     */
    public static MessageEnvelope create(
            String topic,
            String tag,
            String key,
            Map<String, String> headers,
            String payload,
            String traceId,
            String tenantId
    ) {
        return new MessageEnvelope(
                UUID.randomUUID().toString(),
                topic,
                tag,
                key,
                headers,
                payload,
                traceId,
                tenantId,
                Instant.now()
        );
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
