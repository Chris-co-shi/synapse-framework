package com.indigo.synapse.core.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationContextTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void operationActorAttributesShouldBeImmutable() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("source", "security");

        OperationActor actor = new OperationActor(OperationActorType.USER, "u1", "admin", "t1", attributes);
        attributes.put("source", "changed");

        assertEquals("security", actor.attributes().get("source"));
        assertThrows(UnsupportedOperationException.class, () -> actor.attributes().put("x", "y"));
    }

    @Test
    void operationContextAttributesShouldBeImmutable() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("channel", "mq");

        OperationContext context = context(actor("actor"), actor("initiator"), attributes);
        attributes.put("channel", "changed");

        assertEquals("mq", context.attributes().get("channel"));
        assertThrows(UnsupportedOperationException.class, () -> context.attributes().put("x", "y"));
    }

    @Test
    void holderShouldSetReturnAndClearCurrentContext() {
        OperationContext context = context(actor("actor"), null, Map.of());

        OperationContextHolder.set(context);

        assertEquals(context, OperationContextHolder.current().orElseThrow());

        OperationContextHolder.clear();

        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void requireCurrentShouldFailWhenContextIsMissing() {
        assertThrows(IllegalStateException.class, OperationContextHolder::requireCurrent);
    }

    @Test
    void scopeCloseShouldRestorePreviousContext() {
        OperationContext previous = context(actor("previous"), null, Map.of());
        OperationContext current = context(actor("current"), null, Map.of());
        OperationContextHolder.set(previous);

        try (OperationContextScope ignored = OperationContextHolder.scope(current)) {
            assertEquals(current, OperationContextHolder.requireCurrent());
        }

        assertEquals(previous, OperationContextHolder.requireCurrent());
    }

    @Test
    void scopeCloseShouldRestorePreviousContextAfterException() {
        OperationContext previous = context(actor("previous"), null, Map.of());
        OperationContext current = context(actor("current"), null, Map.of());
        OperationContextHolder.set(previous);

        assertThrows(IllegalStateException.class, () -> {
            try (OperationContextScope ignored = OperationContextHolder.scope(current)) {
                throw new IllegalStateException("boom");
            }
        });

        assertEquals(previous, OperationContextHolder.requireCurrent());
    }

    @Test
    void nestedScopeShouldRestoreInOrder() {
        OperationContext first = context(actor("first"), null, Map.of());
        OperationContext second = context(actor("second"), null, Map.of());

        try (OperationContextScope ignored = OperationContextHolder.scope(first)) {
            assertEquals(first, OperationContextHolder.requireCurrent());
            try (OperationContextScope nested = OperationContextHolder.scope(second)) {
                assertEquals(second, OperationContextHolder.requireCurrent());
            }
            assertEquals(first, OperationContextHolder.requireCurrent());
        }

        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void snapshotAndRestoreShouldWork() {
        OperationContext context = context(actor("snapshot"), null, Map.of());
        OperationContextHolder.set(context);
        OperationContextSnapshot snapshot = OperationContextHolder.snapshot();
        OperationContextHolder.clear();

        try (OperationContextScope ignored = OperationContextHolder.restore(snapshot)) {
            assertEquals(context, OperationContextHolder.requireCurrent());
        }

        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void snapshotCodecShouldEncodeAndDecodeContext() {
        OperationContext context = context(actor("actor"), actor("initiator"), Map.of());
        OperationContextSnapshotCodec codec = new OperationContextSnapshotCodec();

        OperationContextSnapshotCarrier carrier = codec.encode(new OperationContextSnapshot(context));
        OperationContext restored = codec.decode(carrier).orElseThrow().context();

        assertEquals("trace-1", carrier.values().get(OperationContextPropagationKeys.TRACE_ID));
        assertEquals("request-1", restored.requestId());
        assertEquals("tenant-1", restored.tenantId());
        assertEquals("actor", restored.actor().id());
        assertEquals("initiator", restored.initiator().id());
        assertEquals("test", restored.source().type());
    }

    @Test
    void snapshotCodecShouldNotCreateActorWhenActorHeadersAreMissing() {
        OperationContextSnapshotCodec codec = new OperationContextSnapshotCodec();
        OperationContextSnapshotCarrier carrier = new OperationContextSnapshotCarrier(Map.of(
                OperationContextPropagationKeys.TRACE_ID, "trace-only",
                OperationContextPropagationKeys.TENANT_ID, "tenant-1"
        ));

        assertTrue(codec.decode(carrier).isEmpty());
    }

    @Test
    void explicitSystemActorFactoryShouldCreateSystemActor() {
        OperationActor system = SystemOperationActorFactory.system("system-job", "System Job");

        assertEquals(OperationActorType.SYSTEM, system.type());
        assertEquals("system-job", system.id());
        assertThrows(IllegalArgumentException.class, () -> SystemOperationActorFactory.system(" ", "bad"));
    }

    @Test
    void contextAwareRunnableShouldPropagateAndRestoreContext() {
        OperationContext captured = context(actor("captured"), null, Map.of());
        OperationContext previous = context(actor("previous"), null, Map.of());
        OperationContextHolder.set(captured);

        Runnable runnable = OperationContextExecutor.wrap(() ->
                assertEquals("captured", OperationContextHolder.requireCurrent().actor().id()));

        OperationContextHolder.set(previous);
        runnable.run();

        assertEquals(previous, OperationContextHolder.requireCurrent());
    }

    @Test
    void contextAwareCallableShouldPropagateContext() throws Exception {
        OperationContext captured = context(actor("callable"), null, Map.of());
        OperationContextHolder.set(captured);
        Callable<String> callable = OperationContextExecutor.wrap(() ->
                OperationContextHolder.requireCurrent().actor().id());
        OperationContextHolder.clear();

        assertEquals("callable", callable.call());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void contextAwareCallableShouldNotPolluteExecutorThread() throws Exception {
        OperationContext captured = context(actor("async"), null, Map.of());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            OperationContextHolder.set(captured);
            Callable<String> callable = OperationContextExecutor.wrap(() ->
                    OperationContextHolder.requireCurrent().actor().id());
            OperationContextHolder.clear();

            Future<String> result = executor.submit(callable);
            Future<Boolean> emptyAfterRun = executor.submit(() -> OperationContextHolder.current().isEmpty());

            assertEquals("async", result.get());
            assertTrue(emptyAfterRun.get());
            assertTrue(OperationContextHolder.current().isEmpty());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void actorAndInitiatorCanBeDifferent() {
        OperationActor actor = new OperationActor(OperationActorType.JOB, "job-1", "sync-job", "t1", Map.of());
        OperationActor initiator = new OperationActor(OperationActorType.USER, "user-1", "admin", "t1", Map.of());

        OperationContext context = context(actor, initiator, Map.of());

        assertNotEquals(context.actor(), context.initiator());
        assertEquals(OperationActorType.JOB, context.actor().type());
        assertEquals(OperationActorType.USER, context.initiator().type());
    }

    @Test
    void emptyContextShouldNotReturnSystemOrUnknownAutomatically() {
        DefaultOperationContextProvider provider = new DefaultOperationContextProvider();

        assertTrue(provider.current().isEmpty());
        assertTrue(provider.currentActorId().isEmpty());

        Set<OperationActorType> automaticTypes = provider.current()
                .map(OperationContext::actor)
                .map(OperationActor::type)
                .map(Set::of)
                .orElse(Set.of());

        assertFalse(automaticTypes.contains(OperationActorType.SYSTEM));
        assertFalse(automaticTypes.contains(OperationActorType.UNKNOWN));
    }

    @Test
    void defaultProviderShouldReturnEmptyWithoutContext() {
        DefaultOperationContextProvider provider = new DefaultOperationContextProvider();

        assertTrue(provider.current().isEmpty());
        assertTrue(provider.currentActorId().isEmpty());
        assertTrue(provider.currentTenantId().isEmpty());
        assertTrue(provider.currentTraceId().isEmpty());
    }

    @Test
    void defaultProviderShouldExposeActorTenantAndTrace() {
        DefaultOperationContextProvider provider = new DefaultOperationContextProvider();
        OperationContext context = context(actor("actor"), null, Map.of());
        OperationContextHolder.set(context);

        assertEquals("actor", provider.currentActorId().orElseThrow());
        assertEquals("tenant-1", provider.currentTenantId().orElseThrow());
        assertEquals("trace-1", provider.currentTraceId().orElseThrow());
    }

    private static OperationActor actor(String id) {
        return new OperationActor(OperationActorType.USER, id, id + "-name", "tenant-1", Map.of("k", "v"));
    }

    private static OperationContext context(OperationActor actor, OperationActor initiator, Map<String, String> attributes) {
        return new OperationContext(
                actor,
                initiator,
                new OperationSource("test", "unit-test", "instance-1", "method", Map.of()),
                "trace-1",
                "tenant-1",
                "request-1",
                Instant.parse("2026-06-13T10:00:00Z"),
                attributes
        );
    }
}
