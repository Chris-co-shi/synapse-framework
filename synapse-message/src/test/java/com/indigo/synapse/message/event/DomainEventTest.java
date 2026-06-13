package com.indigo.synapse.message.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainEventTest {

    @Test
    void shouldCreateEventAndProtectAttributes() {
        DomainEvent event = new DomainEvent(
                "event-1",
                "iam.user.created",
                "USER",
                "1001",
                Instant.parse("2026-05-21T10:00:00Z"),
                "trace-1",
                Map.of("source", "iam"),
                "{\"userId\":\"1001\"}"
        );

        assertEquals("iam", event.attributes().get("source"));
        assertThrows(UnsupportedOperationException.class, () -> event.attributes().put("x", "y"));
    }

    @Test
    void shouldCreateEventByFactory() {
        DomainEvent event = DomainEvent.create(
                "iam.user.disabled",
                "USER",
                "1002",
                "trace-2",
                Map.of(),
                "{}"
        );

        assertFalse(event.eventId().isBlank());
        assertEquals("iam.user.disabled", event.eventType());
    }

    @Test
    void shouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("", "x", "x", "x", Instant.now(), "trace", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("1", "", "x", "x", Instant.now(), "trace", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("1", "x", "", "x", Instant.now(), "trace", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("1", "x", "x", "", Instant.now(), "trace", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("1", "x", "x", "x", null, "trace", Map.of(), null));
        assertThrows(IllegalArgumentException.class, () -> new DomainEvent("1", "x", "x", "x", Instant.now(), "", Map.of(), null));
    }
}
