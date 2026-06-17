package com.indigo.synapse.audit.event;

import com.indigo.synapse.audit.context.AuditContext;
import com.indigo.synapse.audit.context.AuditContextSnapshot;
import com.indigo.synapse.audit.context.AuditContextScope;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.OperationSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuditEventContextEnricherTest {

    private final AuditEventContextEnricher enricher =
            new AuditEventContextEnricher(new DefaultOperationContextProvider());

    @AfterEach
    void tearDown() {
        AuditContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldKeepExplicitSubjectAndTraceId() {
        AuditEvent event = event(new AuditSubject("EXPLICIT", "explicit-1", "tenant-explicit"), "trace-explicit", Map.of());

        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext())) {
            AuditEvent enriched = enricher.enrich(event);

            assertEquals(event.subject(), enriched.subject());
            assertEquals("trace-explicit", enriched.traceId());
        }
    }

    @Test
    void shouldPreferAuditContextSubjectOverOperationActor() {
        AuditContextSnapshot snapshot = new AuditContextSnapshot(
                new AuditSubject("SERVICE", "audit-subject", "tenant-audit"),
                "trace-audit"
        );

        try (AuditContextScope ignored = AuditContext.scope(snapshot);
             OperationContextScope ignoredOperation = OperationContextHolder.scope(operationContext())) {
            AuditEvent enriched = enricher.enrich(event(null, null, Map.of()));

            assertEquals("SERVICE", enriched.subject().subjectType());
            assertEquals("audit-subject", enriched.subject().subjectId());
            assertEquals("tenant-audit", enriched.subject().tenantId());
            assertEquals("trace-audit", enriched.traceId());
        }
    }

    @Test
    void shouldFillSubjectAndTraceIdFromOperationContext() {
        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext())) {
            AuditEvent enriched = enricher.enrich(event(null, null, Map.of()));

            assertEquals("USER", enriched.subject().subjectType());
            assertEquals("actor-1", enriched.subject().subjectId());
            assertEquals("tenant-a", enriched.subject().tenantId());
            assertEquals("trace-1", enriched.traceId());
        }
    }

    @Test
    void shouldFillClientSubjectFromOperationContext() {
        OperationActor actor = new OperationActor(OperationActorType.SERVICE, "client-a", "Client A", "tenant-a", Map.of());
        OperationContext context = new OperationContext(
                actor,
                actor,
                null,
                "trace-client",
                "tenant-a",
                "request-1",
                Instant.parse("2026-05-20T09:59:59Z"),
                Map.of()
        );

        try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
            AuditEvent enriched = enricher.enrich(event(null, null, Map.of()));

            assertEquals("SERVICE", enriched.subject().subjectType());
            assertEquals("client-a", enriched.subject().subjectId());
            assertEquals("tenant-a", enriched.subject().tenantId());
            assertEquals("SERVICE", enriched.attributes().get("operation.actor.type"));
        }
    }

    @Test
    void shouldFillOperationAttributesWithoutOverridingExistingKeys() {
        try (OperationContextScope ignored = OperationContextHolder.scope(operationContext())) {
            AuditEvent enriched = enricher.enrich(event(null, null, Map.of(
                    "operation.requestId", "manual-request",
                    "operation.source.name", "manual-source"
            )));

            assertEquals("USER", enriched.attributes().get("operation.actor.type"));
            assertEquals("actor-1", enriched.attributes().get("operation.actor.id"));
            assertEquals("Alice", enriched.attributes().get("operation.actor.name"));
            assertEquals("SERVICE", enriched.attributes().get("operation.initiator.type"));
            assertEquals("service-1", enriched.attributes().get("operation.initiator.id"));
            assertEquals("Billing Service", enriched.attributes().get("operation.initiator.name"));
            assertEquals("manual-request", enriched.attributes().get("operation.requestId"));
            assertEquals("HTTP", enriched.attributes().get("operation.source.type"));
            assertEquals("manual-source", enriched.attributes().get("operation.source.name"));
            assertEquals("instance-1", enriched.attributes().get("operation.source.instanceId"));
            assertEquals("/users", enriched.attributes().get("operation.source.entrypoint"));
        }
    }

    @Test
    void shouldNotWriteSystemOrUnknownWhenContextMissing() {
        AuditEvent enriched = enricher.enrich(event(null, null, Map.of()));

        assertNull(enriched.subject());
        assertNull(enriched.traceId());
        assertFalse(enriched.attributes().containsValue("SYSTEM"));
        assertFalse(enriched.attributes().containsValue("UNKNOWN"));
    }

    @Test
    void shouldNotWriteSystemOrUnknownActorFromOperationContext() {
        OperationActor actor = new OperationActor(OperationActorType.SYSTEM, "system", "System", null, Map.of());
        OperationActor initiator = new OperationActor(OperationActorType.UNKNOWN, "unknown", "Unknown", null, Map.of());
        OperationContext context = new OperationContext(
                actor,
                initiator,
                null,
                "trace-1",
                null,
                null,
                Instant.parse("2026-05-20T09:59:59Z"),
                Map.of()
        );

        try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
            AuditEvent enriched = enricher.enrich(event(null, null, Map.of()));

            assertNull(enriched.subject());
            assertFalse(enriched.attributes().containsValue("SYSTEM"));
            assertFalse(enriched.attributes().containsValue("UNKNOWN"));
        }
    }

    private static AuditEvent event(AuditSubject subject, String traceId, Map<String, String> attributes) {
        return new AuditEvent(
                "system:user:create",
                subject,
                new AuditTarget("USER", "2"),
                Instant.parse("2026-05-20T10:00:00Z"),
                AuditOutcome.SUCCESS,
                traceId,
                "created user",
                attributes
        );
    }

    private static OperationContext operationContext() {
        OperationActor actor = new OperationActor(OperationActorType.USER, "actor-1", "Alice", "tenant-a", Map.of());
        OperationActor initiator = new OperationActor(
                OperationActorType.SERVICE,
                "service-1",
                "Billing Service",
                "tenant-a",
                Map.of()
        );
        OperationSource source = new OperationSource("HTTP", "admin-api", "instance-1", "/users", Map.of());
        return new OperationContext(
                actor,
                initiator,
                source,
                "trace-1",
                "tenant-a",
                "request-1",
                Instant.parse("2026-05-20T09:59:59Z"),
                Map.of()
        );
    }
}
