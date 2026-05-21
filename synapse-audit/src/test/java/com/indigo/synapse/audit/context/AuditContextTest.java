package com.indigo.synapse.audit.context;

import com.indigo.synapse.audit.event.AuditSubject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditContextTest {

    @AfterEach
    void tearDown() {
        AuditContext.clear();
    }

    @Test
    void shouldStoreAndClearAuditContext() {
        AuditContextSnapshot snapshot = snapshot("1", "trace-1");

        AuditContext.set(snapshot);

        assertEquals(snapshot, AuditContext.current().orElseThrow());
        assertEquals("1", AuditContext.currentSubject().orElseThrow().subjectId());
        assertEquals("trace-1", AuditContext.currentTraceId().orElseThrow());

        AuditContext.clear();

        assertTrue(AuditContext.current().isEmpty());
    }

    @Test
    void scopeShouldRestorePreviousContext() {
        AuditContext.set(snapshot("1", "trace-1"));

        try (AuditContextScope ignored = AuditContext.scope(snapshot("2", "trace-2"))) {
            assertEquals("2", AuditContext.currentSubject().orElseThrow().subjectId());
        }

        assertEquals("1", AuditContext.currentSubject().orElseThrow().subjectId());
    }

    @Test
    void shouldValidateSnapshot() {
        assertThrows(IllegalArgumentException.class, () -> new AuditContextSnapshot(null, "trace"));
        assertThrows(IllegalArgumentException.class, () -> new AuditContextSnapshot(new AuditSubject("USER", "1", null), ""));
    }

    private static AuditContextSnapshot snapshot(String subjectId, String traceId) {
        return new AuditContextSnapshot(new AuditSubject("USER", subjectId, "tenant-a"), traceId);
    }
}
