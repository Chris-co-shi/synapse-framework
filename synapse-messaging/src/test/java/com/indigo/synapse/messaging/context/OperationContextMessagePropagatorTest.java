package com.indigo.synapse.messaging.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationContextMessagePropagatorTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldUseOperationContextProviderWhenWritingHeaders() {
        OperationContext context = context("actor-1", "trace-1", "tenant-a");
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(),
                () -> Optional.of(context)
        );

        MessageEnvelope enriched = propagator.withCurrentContext(envelope(Map.of()));

        assertEquals("trace-1", enriched.headers().get(MessageContextHeaders.TRACE_ID));
        assertEquals("tenant-a", enriched.headers().get(MessageContextHeaders.TENANT_ID));
        assertEquals("USER", enriched.headers().get(MessageContextHeaders.ACTOR_TYPE));
        assertEquals("actor-1", enriched.headers().get(MessageContextHeaders.ACTOR_ID));
    }

    @Test
    void shouldPropagateClientOperationContext() {
        OperationContext context = context(OperationActorType.SERVICE, "client-a", "trace-client", "tenant-a");
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(),
                () -> Optional.of(context)
        );

        MessageEnvelope enriched = propagator.withCurrentContext(envelope(Map.of()));

        assertEquals("SERVICE", enriched.headers().get(MessageContextHeaders.ACTOR_TYPE));
        assertEquals("client-a", enriched.headers().get(MessageContextHeaders.ACTOR_ID));
        assertFalse(enriched.headers().containsKey("Authorization"));
    }

    @Test
    void shouldNotWriteContextHeadersWhenProviderIsEmpty() {
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(),
                Optional::empty
        );

        MessageEnvelope original = envelope(Map.of("x-existing", "value"));
        MessageEnvelope enriched = propagator.withCurrentContext(original);

        assertEquals("value", enriched.headers().get("x-existing"));
        assertFalse(enriched.headers().containsKey(MessageContextHeaders.TRACE_ID));
    }

    @Test
    void shouldNotOverrideExistingHeaders() {
        OperationContext context = context("actor-1", "trace-from-context", "tenant-a");
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator(
                new OperationContextMessageCodec(),
                () -> Optional.of(context)
        );

        MessageEnvelope enriched = propagator.withCurrentContext(envelope(Map.of(
                MessageContextHeaders.TRACE_ID, "trace-existing"
        )));

        assertEquals("trace-existing", enriched.headers().get(MessageContextHeaders.TRACE_ID));
    }

    @Test
    void shouldRestoreContextFromHeadersAndRecoverPreviousContextAfterClose() {
        OperationContext previous = context("previous-actor", "trace-previous", "tenant-previous");
        OperationContextHolder.set(previous);
        OperationContextMessagePropagator propagator = new OperationContextMessagePropagator();
        MessageEnvelope envelope = envelope(Map.of(
                MessageContextHeaders.ACTOR_TYPE, "USER",
                MessageContextHeaders.ACTOR_ID, "actor-1",
                MessageContextHeaders.ACTOR_NAME, "Actor One",
                MessageContextHeaders.TRACE_ID, "trace-1",
                MessageContextHeaders.TENANT_ID, "tenant-a"
        ));

        try (OperationContextScope ignored = propagator.restore(envelope)) {
            OperationContext restored = OperationContextHolder.current().orElseThrow();
            assertEquals("actor-1", restored.actor().id());
            assertEquals("trace-1", restored.traceId());
            assertEquals("tenant-a", restored.tenantId());
        }

        assertEquals("previous-actor", OperationContextHolder.current().orElseThrow().actor().id());
    }

    @Test
    void shouldNotRestoreContextWhenActorTypeIsMissing() {
        OperationContextMessageCodec codec = new OperationContextMessageCodec();
        Map<String, String> headers = Map.of(
                MessageContextHeaders.ACTOR_ID, "actor-1",
                MessageContextHeaders.TRACE_ID, "trace-1"
        );

        assertTrue(codec.decode(headers).isEmpty());
    }

    @Test
    void shouldValidateConstructorArguments() {
        OperationContextMessageCodec codec = new OperationContextMessageCodec();
        OperationContextProvider provider = Optional::empty;

        assertThrows(IllegalArgumentException.class, () -> new OperationContextMessagePropagator(null, provider));
        assertThrows(IllegalArgumentException.class, () -> new OperationContextMessagePropagator(codec, null));
        assertThrows(IllegalArgumentException.class, () -> new OperationContextMessagePropagator().withCurrentContext(null));
        assertThrows(IllegalArgumentException.class, () -> new OperationContextMessagePropagator().restore(null));
    }

    private static OperationContext context(String actorId, String traceId, String tenantId) {
        return context(OperationActorType.USER, actorId, traceId, tenantId);
    }

    private static OperationContext context(OperationActorType actorType, String actorId, String traceId, String tenantId) {
        OperationActor actor = new OperationActor(actorType, actorId, "Actor", tenantId, Map.of());
        return new OperationContext(
                actor,
                actor,
                null,
                traceId,
                tenantId,
                "request-1",
                Instant.parse("2026-06-14T00:00:00Z"),
                Map.of()
        );
    }

    private static MessageEnvelope envelope(Map<String, String> headers) {
        return new MessageEnvelope(
                "message-1",
                "sample.created",
                "sample-topic",
                "sample-tag",
                "sample-key",
                "sample-idempotent",
                "sample-service",
                "application/json",
                "v1",
                headers,
                "{}",
                "trace-envelope",
                "tenant-envelope",
                Instant.parse("2026-06-14T00:00:00Z"),
                Instant.parse("2026-06-14T00:00:01Z")
        );
    }
}
