package com.indigo.synapse.mq.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageEnvelopeTest {

    @Test
    void shouldValidateRequiredFields() {
        Instant now = Instant.parse("2026-06-14T00:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> envelope("", "sample.created", "sample-topic", now));
        assertThrows(IllegalArgumentException.class, () -> envelope("message-1", "", "sample-topic", now));
        assertThrows(IllegalArgumentException.class, () -> envelope("message-1", "sample.created", "", now));
        assertThrows(IllegalArgumentException.class, () -> envelope("message-1", "sample.created", "sample-topic", null));
    }

    @Test
    void shouldNormalizeHeaders() {
        MessageEnvelope emptyHeaders = envelope("message-1", "sample.created", "sample-topic",
                Instant.parse("2026-06-14T00:00:00Z"), null);

        assertTrue(emptyHeaders.headers().isEmpty());

        Map<String, String> source = new HashMap<>();
        source.put("x-sample", "value");
        MessageEnvelope copiedHeaders = envelope("message-2", "sample.updated", "sample-topic",
                Instant.parse("2026-06-14T00:00:01Z"), source);
        source.put("x-sample", "changed");

        assertEquals("value", copiedHeaders.headers().get("x-sample"));
        assertThrows(UnsupportedOperationException.class, () -> copiedHeaders.headers().put("x", "y"));
    }

    @Test
    void shouldCreateEnvelopeWithGeneratedMessageIdAndCreatedAt() {
        MessageEnvelope envelope = MessageEnvelope.create(
                "sample.created",
                "sample-topic",
                "sample-tag",
                "sample-key",
                "sample-idempotent",
                "sample-service",
                "application/json",
                "v1",
                Map.of(),
                "{}",
                "trace-1",
                "tenant-a",
                Instant.parse("2026-06-14T00:00:00Z")
        );

        assertNotNull(envelope.messageId());
        assertFalse(envelope.messageId().isBlank());
        assertEquals("sample.created", envelope.messageType());
        assertEquals("sample-topic", envelope.topic());
        assertEquals("sample-idempotent", envelope.idempotentKey());
        assertNotNull(envelope.createdAt());
    }

    private MessageEnvelope envelope(String messageId, String messageType, String topic, Instant createdAt) {
        return envelope(messageId, messageType, topic, createdAt, Map.of());
    }

    private MessageEnvelope envelope(
            String messageId,
            String messageType,
            String topic,
            Instant createdAt,
            Map<String, String> headers
    ) {
        return new MessageEnvelope(
                messageId,
                messageType,
                topic,
                "sample-tag",
                "sample-key",
                "sample-idempotent",
                "sample-service",
                "application/json",
                "v1",
                headers,
                "{}",
                "trace-1",
                "tenant-a",
                Instant.parse("2026-06-14T00:00:00Z"),
                createdAt
        );
    }
}
