package com.indigo.synapse.message.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        String eventId,
        String eventType,
        String aggregateType,
        String aggregateId,
        Instant occurredAt,
        String traceId,
        Map<String, String> attributes,
        String payload
) {

    public DomainEvent {
        validate(eventId, "eventId");
        validate(eventType, "eventType");
        validate(aggregateType, "aggregateType");
        validate(aggregateId, "aggregateId");
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        validate(traceId, "traceId");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static DomainEvent create(
            String eventType,
            String aggregateType,
            String aggregateId,
            String traceId,
            Map<String, String> attributes,
            String payload
    ) {
        return new DomainEvent(
                UUID.randomUUID().toString(),
                eventType,
                aggregateType,
                aggregateId,
                Instant.now(),
                traceId,
                attributes,
                payload
        );
    }

    private static void validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
