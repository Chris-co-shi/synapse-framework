package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditLogPortTest {

    @Test
    void noopPortShouldValidateEvent() {
        NoopAuditLogPort port = new NoopAuditLogPort();

        port.record(event());

        assertThrows(IllegalArgumentException.class, () -> port.record(null));
    }

    @Test
    void compositePortShouldRecordInAllDelegates() {
        List<AuditEvent> first = new ArrayList<>();
        List<AuditEvent> second = new ArrayList<>();
        CompositeAuditLogPort port = new CompositeAuditLogPort(List.of(first::add, second::add));
        AuditEvent event = event();

        port.record(event);

        assertEquals(List.of(event), first);
        assertEquals(List.of(event), second);
    }

    @Test
    void compositePortShouldRejectInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeAuditLogPort(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new CompositeAuditLogPort(List.of(event -> {
        })).record(null));
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
                Map.of()
        );
    }
}
