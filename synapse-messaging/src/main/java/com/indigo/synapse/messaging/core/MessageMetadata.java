package com.indigo.synapse.messaging.core;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 消息身份、类型和传播信息。
 *
 * @param messageId 每次消息发布的唯一标识
 * @param eventId 领域事件的稳定标识；同一事件重投时保持不变，可为空
 * @param messageType Handler 路由使用的稳定类型
 * @param sourceService 产生消息的服务名
 * @param contentType 负载媒体类型
 * @param schemaVersion 负载结构版本
 * @param headers Broker 中立的字符串 header
 * @param occurredAt 事件实际发生时间
 * @param createdAt Envelope 创建时间
 */
public record MessageMetadata(
        String messageId,
        String eventId,
        String messageType,
        String sourceService,
        String contentType,
        MessageVersion schemaVersion,
        Map<String, String> headers,
        Instant occurredAt,
        Instant createdAt
) {
    public MessageMetadata {
        messageId = requireText(messageId, "messageId");
        eventId = trimToNull(eventId);
        messageType = requireText(messageType, "messageType");
        sourceService = requireText(sourceService, "sourceService");
        contentType = requireText(contentType, "contentType");
        schemaVersion = schemaVersion == null ? MessageVersion.V1 : schemaVersion;
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    /** 创建带随机 messageId 的元数据。 */
    public static MessageMetadata create(String eventId, String messageType, String sourceService) {
        return new MessageMetadata(UUID.randomUUID().toString(), eventId, messageType, sourceService,
                "application/json", MessageVersion.V1, Map.of(), Instant.now(), Instant.now());
    }

    /** 创建仅替换 header 的副本。 */
    public MessageMetadata withHeaders(Map<String, String> newHeaders) {
        return new MessageMetadata(messageId, eventId, messageType, sourceService, contentType,
                schemaVersion, newHeaders, occurredAt, createdAt);
    }

    private static String requireText(String value, String name) {
        String normalized = trimToNull(value);
        if (normalized == null) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
