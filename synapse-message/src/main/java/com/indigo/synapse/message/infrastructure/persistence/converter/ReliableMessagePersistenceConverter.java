package com.indigo.synapse.message.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.core.ReliableMessageStatus;
import com.indigo.synapse.message.infrastructure.persistence.entity.ReliableMessageEntity;

import java.util.Map;

/**
 * 可靠消息持久化模型转换器。
 */
public final class ReliableMessagePersistenceConverter {

    private static final TypeReference<Map<String, String>> HEADERS_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ReliableMessagePersistenceConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? defaultObjectMapper() : objectMapper;
    }

    public ReliableMessageEntity toEntity(ReliableMessage message) {
        ReliableMessageEntity entity = new ReliableMessageEntity();
        entity.setMessageId(message.messageId());
        entity.setTopic(message.envelope().topic());
        entity.setTag(message.envelope().tag());
        entity.setKey(message.envelope().key());
        entity.setHeadersJson(writeHeaders(message.envelope().headers()));
        entity.setPayload(message.envelope().payload());
        entity.setTraceId(message.envelope().traceId());
        entity.setTenantId(message.envelope().tenantId());
        entity.setMessageCreatedAt(message.envelope().createdAt());
        entity.setStatus(message.status().name());
        entity.setAttempt(message.attempt());
        entity.setNextRetryAt(message.nextRetryAt());
        entity.setLockedBy(message.lockedBy());
        entity.setLockedUntil(message.lockedUntil());
        entity.setLastError(message.lastError());
        entity.setIdempotencyKey(message.idempotencyKey());
        entity.setCreatedAt(message.createdAt());
        entity.setUpdatedAt(message.updatedAt());
        entity.setVersion(message.version());
        return entity;
    }

    public ReliableMessage toDomain(ReliableMessageEntity entity) {
        MessageEnvelope envelope = new MessageEnvelope(
                entity.getMessageId(),
                entity.getTopic(),
                entity.getTag(),
                entity.getKey(),
                readHeaders(entity.getHeadersJson()),
                entity.getPayload(),
                entity.getTraceId(),
                entity.getTenantId(),
                entity.getMessageCreatedAt()
        );
        return new ReliableMessage(
                entity.getMessageId(),
                envelope,
                ReliableMessageStatus.valueOf(entity.getStatus()),
                entity.getAttempt() == null ? 0 : entity.getAttempt(),
                entity.getNextRetryAt(),
                entity.getLockedBy(),
                entity.getLockedUntil(),
                entity.getLastError(),
                entity.getIdempotencyKey(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion() == null ? 0 : entity.getVersion()
        );
    }

    private String writeHeaders(Map<String, String> headers) {
        try {
            return objectMapper.writeValueAsString(headers == null ? Map.of() : headers);
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to write message headers", exception);
        }
    }

    private Map<String, String> readHeaders(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, HEADERS_TYPE);
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to read message headers", exception);
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
