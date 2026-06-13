package com.indigo.synapse.message.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextSnapshot;
import com.indigo.synapse.core.context.OperationSource;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationContextMessageCodecTest {

    private final OperationContextMessageCodec codec = new OperationContextMessageCodec(
            Clock.fixed(Instant.parse("2026-05-20T10:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void shouldEncodeOperationContextHeaders() {
        Map<String, String> headers = codec.encode(new OperationContextSnapshot(context(OperationActorType.USER)));

        assertEquals("trace-1", headers.get(MessageContextHeaders.TRACE_ID));
        assertEquals("request-1", headers.get(MessageContextHeaders.REQUEST_ID));
        assertEquals("tenant-a", headers.get(MessageContextHeaders.TENANT_ID));
        assertEquals("USER", headers.get(MessageContextHeaders.ACTOR_TYPE));
        assertEquals("actor-1", headers.get(MessageContextHeaders.ACTOR_ID));
        assertEquals("Alice", headers.get(MessageContextHeaders.ACTOR_NAME));
        assertEquals("SERVICE", headers.get(MessageContextHeaders.INITIATOR_TYPE));
        assertEquals("service-1", headers.get(MessageContextHeaders.INITIATOR_ID));
        assertEquals("Billing Service", headers.get(MessageContextHeaders.INITIATOR_NAME));
        assertEquals("HTTP", headers.get(MessageContextHeaders.SOURCE_TYPE));
        assertEquals("admin-api", headers.get(MessageContextHeaders.SOURCE_NAME));
        assertEquals("instance-1", headers.get(MessageContextHeaders.SOURCE_INSTANCE_ID));
        assertEquals("/users", headers.get(MessageContextHeaders.SOURCE_ENTRYPOINT));
    }

    @Test
    void shouldEncodeExplicitJobAndSystemActorTypes() {
        assertEquals("JOB", codec.encode(new OperationContextSnapshot(context(OperationActorType.JOB)))
                .get(MessageContextHeaders.ACTOR_TYPE));
        assertEquals("SYSTEM", codec.encode(new OperationContextSnapshot(context(OperationActorType.SYSTEM)))
                .get(MessageContextHeaders.ACTOR_TYPE));
    }

    @Test
    void shouldNotPropagateAttributesRolesOrPermissions() {
        Map<String, String> headers = codec.encode(new OperationContextSnapshot(context(OperationActorType.USER)));

        assertFalse(headers.containsKey("roles"));
        assertFalse(headers.containsKey("permissions"));
        assertFalse(headers.containsKey("custom"));
    }

    @Test
    void shouldDecodeOperationContextFromHeaders() {
        Map<String, String> headers = codec.encode(new OperationContextSnapshot(context(OperationActorType.USER)));

        OperationContext decoded = codec.decode(headers).orElseThrow().context();

        assertEquals("trace-1", decoded.traceId());
        assertEquals("request-1", decoded.requestId());
        assertEquals("tenant-a", decoded.tenantId());
        assertEquals(OperationActorType.USER, decoded.actor().type());
        assertEquals("actor-1", decoded.actor().id());
        assertEquals("Alice", decoded.actor().name());
        assertEquals(OperationActorType.SERVICE, decoded.initiator().type());
        assertEquals("HTTP", decoded.source().type());
        assertEquals(Instant.parse("2026-05-20T10:00:00Z"), decoded.occurredAt());
    }

    @Test
    void shouldDecodeContextWithoutActor() {
        Optional<OperationContextSnapshot> snapshot = codec.decode(Map.of(
                MessageContextHeaders.TRACE_ID, "trace-1",
                MessageContextHeaders.TENANT_ID, "tenant-a"
        ));

        OperationContext context = snapshot.orElseThrow().context();
        assertNull(context.actor());
        assertEquals("trace-1", context.traceId());
        assertEquals("tenant-a", context.tenantId());
    }

    @Test
    void shouldReturnEmptyWhenHeadersHaveNoContextInformation() {
        assertTrue(codec.decode(Map.of("x-other", "value")).isEmpty());
        assertTrue(codec.encode(new OperationContextSnapshot(null)).isEmpty());
    }

    private static OperationContext context(OperationActorType actorType) {
        OperationActor actor = new OperationActor(
                actorType,
                "actor-1",
                "Alice",
                "tenant-a",
                Map.of("roles", "admin")
        );
        OperationActor initiator = new OperationActor(
                OperationActorType.SERVICE,
                "service-1",
                "Billing Service",
                "tenant-a",
                Map.of("permissions", "message:send")
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
                Map.of("custom", "value")
        );
    }
}
