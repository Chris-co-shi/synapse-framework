package com.indigo.synapse.message.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.core.context.OperationSource;
import com.indigo.synapse.message.core.MessageEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationContextMessagePropagatorTest {

    private final OperationContextMessagePropagator propagator = new OperationContextMessagePropagator();

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldWriteCurrentContextHeadersWithoutOverridingExistingHeaders() {
        MessageEnvelope envelope = envelope(Map.of(
                MessageContextHeaders.TRACE_ID, "manual-trace",
                MessageContextHeaders.ACTOR_ID, "manual-actor"
        ));

        try (OperationContextScope ignored = OperationContextHolder.scope(context("actor-1", "trace-1"))) {
            MessageEnvelope enriched = propagator.withCurrentContext(envelope);

            assertEquals("manual-trace", enriched.headers().get(MessageContextHeaders.TRACE_ID));
            assertEquals("manual-actor", enriched.headers().get(MessageContextHeaders.ACTOR_ID));
            assertEquals("request-1", enriched.headers().get(MessageContextHeaders.REQUEST_ID));
            assertEquals("tenant-a", enriched.headers().get(MessageContextHeaders.TENANT_ID));
            assertEquals("USER", enriched.headers().get(MessageContextHeaders.ACTOR_TYPE));
            assertEquals("Alice", enriched.headers().get(MessageContextHeaders.ACTOR_NAME));
            assertEquals("SERVICE", enriched.headers().get(MessageContextHeaders.INITIATOR_TYPE));
            assertEquals("service-1", enriched.headers().get(MessageContextHeaders.INITIATOR_ID));
            assertEquals("HTTP", enriched.headers().get(MessageContextHeaders.SOURCE_TYPE));
        }
    }

    @Test
    void shouldReturnSameEnvelopeWhenCurrentContextMissing() {
        MessageEnvelope envelope = envelope(Map.of());

        MessageEnvelope enriched = propagator.withCurrentContext(envelope);

        assertSame(envelope, enriched);
        assertTrue(enriched.headers().values().stream().noneMatch(value -> "SYSTEM".equals(value) || "UNKNOWN".equals(value)));
    }

    @Test
    void shouldRestoreContextFromMessageHeadersAndRecoverPreviousOnClose() {
        MessageEnvelope envelope;
        try (OperationContextScope ignored = OperationContextHolder.scope(context("actor-1", "trace-1"))) {
            envelope = propagator.withCurrentContext(envelope(Map.of()));
        }
        OperationContext previous = context("previous", "trace-previous");
        OperationContextHolder.set(previous);

        try (OperationContextScope ignored = propagator.restore(envelope)) {
            OperationContext restored = OperationContextHolder.requireCurrent();
            assertEquals("actor-1", restored.actor().id());
            assertEquals("trace-1", restored.traceId());
        }

        assertEquals("previous", OperationContextHolder.requireCurrent().actor().id());
    }

    @Test
    void shouldNotCreateActorWhenHeadersDoNotContainActor() {
        MessageEnvelope envelope = envelope(Map.of(MessageContextHeaders.TRACE_ID, "trace-only"));

        try (OperationContextScope ignored = propagator.restore(envelope)) {
            OperationContext restored = OperationContextHolder.requireCurrent();
            assertNull(restored.actor());
            assertEquals("trace-only", restored.traceId());
        }
    }

    private static MessageEnvelope envelope(Map<String, String> headers) {
        return new MessageEnvelope(
                "message-1",
                "topic-1",
                "tag",
                "key",
                headers,
                "payload",
                null,
                null,
                Instant.parse("2026-05-20T10:00:00Z")
        );
    }

    private static OperationContext context(String actorId, String traceId) {
        OperationActor actor = new OperationActor(OperationActorType.USER, actorId, "Alice", "tenant-a", Map.of());
        OperationActor initiator = new OperationActor(OperationActorType.SERVICE, "service-1", "Billing Service", "tenant-a", Map.of());
        OperationSource source = new OperationSource("HTTP", "admin-api", "instance-1", "/users", Map.of());
        return new OperationContext(
                actor,
                initiator,
                source,
                traceId,
                "tenant-a",
                "request-1",
                Instant.parse("2026-05-20T09:59:59Z"),
                Map.of()
        );
    }
}
