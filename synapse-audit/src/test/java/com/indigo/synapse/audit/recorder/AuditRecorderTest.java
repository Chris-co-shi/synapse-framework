package com.indigo.synapse.audit.recorder;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditSubject;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditRecorderTest {

    @Test
    void shouldRecordAuditEventThroughPort() {
        CapturingAuditLogPort port = new CapturingAuditLogPort();
        AuditRecorder recorder = new AuditRecorder(port);
        AuditEvent event = event();

        recorder.record(event);

        assertEquals(event.action(), port.event.action());
        org.junit.jupiter.api.Assertions.assertNotNull(port.event.eventId());
    }

    @Test
    void shouldNotSwallowPortException() {
        AuditRecorder recorder = new AuditRecorder(event -> {
            throw new IllegalStateException("audit store unavailable");
        });

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> recorder.record(event()));

        assertEquals("audit store unavailable", error.getMessage());
    }

    @Test
    void shouldValidateRecorderInput() {
        AuditRecorder recorder = new AuditRecorder(event -> {
        });

        assertThrows(IllegalArgumentException.class, () -> new AuditRecorder(null));
        assertThrows(IllegalArgumentException.class, () -> new AuditRecorder(event -> {
        }, null));
        assertThrows(IllegalArgumentException.class, () -> recorder.record(null));
    }

    @Test
    void shouldRejectEventMissingSubjectBeforeWritingPort() {
        CapturingAuditLogPort port = new CapturingAuditLogPort();
        AuditRecorder recorder = new AuditRecorder(
                port,
                new AuditEventContextEnricher(new DefaultOperationContextProvider())
        );
        AuditEvent event = new AuditEvent(
                "system:user:create",
                null,
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-1",
                "created user",
                Map.of()
        );

        assertThrows(IllegalArgumentException.class, () -> recorder.record(event));
        assertNull(port.event);
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

    private static final class CapturingAuditLogPort implements AuditLogPort {

        private AuditEvent event;

        @Override
        public void record(AuditEvent event) {
            this.event = event;
        }
    }
}
