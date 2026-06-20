package com.indigo.synapse.messaging.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.messaging.MessageFixtures;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OperationContextMessagePropagatorTest {
    @AfterEach
    void clearContext() { OperationContextHolder.clear(); }

    @Test
    void shouldWriteProviderContextWithoutSecurityCredentials() {
        OperationContext context = context("actor-1", "trace-1", "tenant-a");
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(), () -> Optional.of(context));

        MessageEnvelope enriched = propagator.withCurrentContext(MessageFixtures.envelope());

        assertThat(enriched.metadata().headers())
                .containsEntry(MessageContextHeaders.TRACE_ID, "trace-1")
                .containsEntry(MessageContextHeaders.ACTOR_ID, "actor-1")
                .doesNotContainKeys("Authorization", "roles", "permissions");
    }

    @Test
    void shouldPreserveExplicitEnvelopeHeaders() {
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(), () -> Optional.of(context("actor-1", "context-trace", "tenant-a")));
        MessageEnvelope original = MessageFixtures.envelope();
        original = new MessageEnvelope(original.metadata().withHeaders(Map.of(
                MessageContextHeaders.TRACE_ID, "explicit-trace")), original.destination(), original.payload());

        assertThat(propagator.withCurrentContext(original).metadata().headers())
                .containsEntry(MessageContextHeaders.TRACE_ID, "explicit-trace");
    }

    @Test
    void shouldRestoreMessageContextAndThenRecoverPreviousContext() {
        OperationContextHolder.set(context("previous", "previous-trace", "tenant-old"));
        MessageEnvelope original = MessageFixtures.envelope();
        MessageEnvelope incoming = new MessageEnvelope(original.metadata().withHeaders(Map.of(
                MessageContextHeaders.ACTOR_TYPE, "USER",
                MessageContextHeaders.ACTOR_ID, "incoming",
                MessageContextHeaders.TRACE_ID, "incoming-trace",
                MessageContextHeaders.TENANT_ID, "tenant-new")), original.destination(), original.payload());

        try (OperationContextScope ignored = new OperationContextMessagePropagator().restore(incoming)) {
            assertThat(OperationContextHolder.current().orElseThrow().actor().id()).isEqualTo("incoming");
        }
        assertThat(OperationContextHolder.current().orElseThrow().actor().id()).isEqualTo("previous");
    }

    private static OperationContext context(String actorId, String traceId, String tenantId) {
        OperationActor actor = new OperationActor(OperationActorType.USER, actorId, "Actor", tenantId, Map.of());
        return new OperationContext(actor, actor, null, traceId, tenantId, "request-1",
                Instant.parse("2026-06-20T00:00:00Z"), Map.of());
    }
}
