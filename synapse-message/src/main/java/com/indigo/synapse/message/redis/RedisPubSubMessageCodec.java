package com.indigo.synapse.message.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.indigo.synapse.message.core.MessageEnvelope;

/**
 * Redis Pub/Sub 消息外壳 JSON 编解码器。
 */
public final class RedisPubSubMessageCodec {

    private final ObjectMapper objectMapper;

    public RedisPubSubMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper == null ? defaultObjectMapper() : objectMapper;
    }

    public String encode(MessageEnvelope message) {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        try {
            return objectMapper.writeValueAsString(message);
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to encode message envelope", exception);
        }
    }

    public MessageEnvelope decode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("message value must not be blank");
        }
        try {
            return objectMapper.readValue(value, MessageEnvelope.class);
        } catch (Exception exception) {
            throw new IllegalArgumentException("failed to decode message envelope", exception);
        }
    }

    private static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
