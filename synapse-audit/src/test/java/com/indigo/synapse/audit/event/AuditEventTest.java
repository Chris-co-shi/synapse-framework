package com.indigo.synapse.audit.event;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuditEventTest {

    @Test
    void shouldCreateAuditEventAndMaskSensitiveAttributes() {
        AuditEvent event = new AuditEvent(
                "system:user:create",
                new AuditSubject("USER", "1", "tenant-a"),
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                "trace-1",
                "created user",
                Map.of("username", "alice", "accessToken", "token-value", "password", "secret")
        );

        assertEquals("alice", event.attributes().get("username"));
        assertEquals(SensitiveAuditValueMasker.MASKED, event.attributes().get("accessToken"));
        assertEquals(SensitiveAuditValueMasker.MASKED, event.attributes().get("password"));
        assertThrows(UnsupportedOperationException.class, () -> event.attributes().put("x", "y"));
    }

    @Test
    void shouldBuildAuditEventWithBuilder() {
        AuditEvent event = AuditEvent.builder()
                .action("system:user:disable")
                .subject(new AuditSubject("USER", "1", "tenant-a"))
                .target(new AuditTarget("USER", "2"))
                .occurredAt(Instant.parse("2026-05-20T10:00:00Z"))
                .outcome(AuditOutcome.FAILURE)
                .traceId("trace-1")
                .message("disabled user failed")
                .attributes(Map.of("secretKey", "plain"))
                .build();

        assertEquals("system:user:disable", event.action());
        assertEquals(AuditOutcome.FAILURE, event.outcome());
        assertEquals(SensitiveAuditValueMasker.MASKED, event.attributes().get("secretKey"));
    }

    @Test
    void shouldRejectInvalidAuditEvent() {
        AuditSubject subject = new AuditSubject("USER", "1", null);
        AuditTarget target = new AuditTarget("USER", "2");
        Instant now = Instant.parse("2026-05-20T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("", subject, target, now, AuditOutcome.SUCCESS, "trace", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("a", null, target, now, AuditOutcome.SUCCESS, "trace", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("a", subject, null, now, AuditOutcome.SUCCESS, "trace", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("a", subject, target, null, AuditOutcome.SUCCESS, "trace", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("a", subject, target, now, null, "trace", null, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new AuditEvent("a", subject, target, now, AuditOutcome.SUCCESS, "", null, Map.of()));
    }
}
