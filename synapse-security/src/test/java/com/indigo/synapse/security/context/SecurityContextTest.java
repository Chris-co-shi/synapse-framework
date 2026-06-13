package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityContextTest {

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldStoreAndClearCurrentUser() {
        LoginUser user = new LoginUser("1", "admin", "tenant-a", Set.of(), Set.of());

        SecurityContext.set(user);

        assertEquals(user, SecurityContext.currentUser().orElseThrow());
        OperationContext operationContext = OperationContextHolder.current().orElseThrow();
        assertEquals(OperationActorType.USER, operationContext.actor().type());
        assertEquals("1", operationContext.actor().id());
        assertEquals("admin", operationContext.actor().name());
        assertEquals("tenant-a", operationContext.actor().tenantId());
        assertEquals(operationContext.actor(), operationContext.initiator());
        assertEquals("tenant-a", operationContext.tenantId());

        SecurityContext.clear();

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearWhenSetNull() {
        SecurityContext.set(new LoginUser("1", "admin", null, Set.of(), Set.of()));

        SecurityContext.set(null);

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearOperationContextScopeWhenEmpty() {
        SecurityContext.set(new LoginUser("1", "admin", null, Set.of(), Set.of()));
        SecurityContext.clear();

        SecurityContext.clearIfEmpty();

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRestorePreviousOperationContextWhenCleared() {
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");
        LoginUser user = new LoginUser("1", "admin", "tenant-a", Set.of("ADMIN"), Set.of("system:user:list"));

        try (OperationContextScope ignored = OperationContextHolder.scope(jobContext)) {
            SecurityContext.set(user);

            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());

            SecurityContext.clear();

            assertEquals(OperationActorType.JOB, OperationContextHolder.requireCurrent().actor().type());
            assertEquals("job-1", OperationContextHolder.requireCurrent().actor().id());
        }
    }

    private static OperationContext context(OperationActorType actorType, String actorId) {
        OperationActor actor = new OperationActor(actorType, actorId, actorId + "-name", "tenant-a", Map.of());
        return new OperationContext(
                actor,
                null,
                null,
                null,
                "tenant-a",
                null,
                Instant.parse("2026-06-13T10:00:00Z"),
                Map.of()
        );
    }
}
