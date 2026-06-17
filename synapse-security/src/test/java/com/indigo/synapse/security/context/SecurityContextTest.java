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
        AuthenticatedUser user = new AuthenticatedUser("1", "admin", "tenant-a", Set.of(), Set.of());

        SecurityContext.set(user);

        assertEquals(user, SecurityContext.currentPrincipal().orElseThrow());
        assertEquals(user, SecurityContext.currentUser().orElseThrow());
        assertTrue(SecurityContext.currentClient().isEmpty());
        OperationContext operationContext = OperationContextHolder.current().orElseThrow();
        assertEquals(OperationActorType.USER, operationContext.actor().type());
        assertEquals("1", operationContext.actor().id());
        assertEquals("admin", operationContext.actor().name());
        assertEquals("tenant-a", operationContext.actor().tenantId());
        assertEquals(operationContext.actor(), operationContext.initiator());
        assertEquals("tenant-a", operationContext.tenantId());

        SecurityContext.clear();

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(SecurityContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldStoreClientWithoutMappingToUser() {
        AuthenticatedClient client = new AuthenticatedClient(
                "client-a",
                "message-service",
                "tenant-a",
                Set.of("INTERNAL"),
                Set.of("message:send")
        );

        try (SecurityContextScope ignored = SecurityContext.openScope(client)) {
            assertEquals(client, SecurityContext.currentPrincipal().orElseThrow());
            assertEquals(client, SecurityContext.currentClient().orElseThrow());
            assertTrue(SecurityContext.currentUser().isEmpty());

            OperationContext operationContext = OperationContextHolder.current().orElseThrow();
            assertEquals(OperationActorType.SERVICE, operationContext.actor().type());
            assertEquals("client-a", operationContext.actor().id());
            assertEquals("message-service", operationContext.actor().name());
            assertTrue(operationContext.actor().attributes().isEmpty());
        }

        assertTrue(SecurityContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRestoreNestedSecurityAndOperationContextScopes() {
        AuthenticatedUser user = new AuthenticatedUser("1", "admin", "tenant-a", Set.of(), Set.of("a"));
        AuthenticatedClient client = new AuthenticatedClient("client-a", "client-a", "tenant-b", Set.of(), Set.of("b"));

        try (SecurityContextScope userScope = SecurityContext.openScope(user)) {
            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());
            assertEquals(user, SecurityContext.currentUser().orElseThrow());

            try (SecurityContextScope clientScope = SecurityContext.openScope(client)) {
                assertEquals(OperationActorType.SERVICE, OperationContextHolder.requireCurrent().actor().type());
                assertEquals(client, SecurityContext.currentClient().orElseThrow());
                assertTrue(SecurityContext.currentUser().isEmpty());
            }

            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());
            assertEquals(user, SecurityContext.currentUser().orElseThrow());
        }

        assertTrue(SecurityContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearWhenSetNull() {
        SecurityContext.set(new AuthenticatedUser("1", "admin", null, Set.of(), Set.of()));

        SecurityContext.set(null);

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldClearOperationContextScopeWhenEmpty() {
        SecurityContext.set(new AuthenticatedUser("1", "admin", null, Set.of(), Set.of()));
        SecurityContext.clear();

        SecurityContext.clearIfEmpty();

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRestorePreviousOperationContextWhenCleared() {
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");
        AuthenticatedUser user = new AuthenticatedUser("1", "admin", "tenant-a", Set.of("ADMIN"), Set.of("system:user:list"));

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
