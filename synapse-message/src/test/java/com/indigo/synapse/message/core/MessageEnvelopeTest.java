package com.indigo.synapse.message.core;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageEnvelopeTest {

    @Test
    void shouldCreateMessageAndProtectHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-trace-id", "trace-1");

        MessageEnvelope message = new MessageEnvelope(
                "message-1",
                "topic-1",
                "tag-1",
                "key-1",
                headers,
                "{}",
                "trace-1",
                "tenant-1",
                Instant.parse("2026-06-13T00:00:00Z")
        );

        headers.put("x-trace-id", "changed");

        assertEquals("trace-1", message.headers().get("x-trace-id"));
        assertThrows(UnsupportedOperationException.class, () -> message.headers().put("x", "y"));
    }

    @Test
    void shouldCreateMessageByFactory() {
        MessageEnvelope message = MessageEnvelope.create("topic-1", null, null, Map.of(), "payload", null, null);

        assertFalse(message.messageId().isBlank());
        assertEquals("topic-1", message.topic());
        assertEquals("payload", message.payload());
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new MessageEnvelope("", "topic", null, null, Map.of(), null, null, null, Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new MessageEnvelope("id", "", null, null, Map.of(), null, null, null, Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new MessageEnvelope("id", "topic", null, null, Map.of(), null, null, null, null));
    }
}
