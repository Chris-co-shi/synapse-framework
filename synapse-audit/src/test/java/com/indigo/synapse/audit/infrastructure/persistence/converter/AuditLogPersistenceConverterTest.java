package com.indigo.synapse.audit.infrastructure.persistence.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditLogPersistenceConverterTest {

    private final AuditLogPersistenceConverter converter = new AuditLogPersistenceConverter(new ObjectMapper());

    @Test
    void shouldConvertAuditEventToEntity() {
        AuditEvent event = event();

        var entity = converter.toEntity(event);

        assertEquals("system:user:create", entity.getAction());
        assertEquals("{\"username\":\"alice\"}", entity.getAttributesJson());
    }

    @Test
    void shouldRejectNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> converter.toEntity(null));
    }

    private static AuditEvent event() {
        return new AuditEvent(
                "system:user:create",
                new AuditSubject("USER", "1", "tenant-a"),
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-1",
                "created user",
                Map.of("username", "alice")
        );
    }
}
