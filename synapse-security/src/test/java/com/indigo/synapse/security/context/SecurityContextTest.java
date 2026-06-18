package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextTest {

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
    void shouldRestoreOuterContextsAfterNullSecurityScope() {
        OperationContext jobContext = jobContext();
        AuthenticatedUser user = new AuthenticatedUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );

        try (OperationContextScope jobScope =
                     OperationContextHolder.scope(jobContext)) {

            assertEquals(jobContext, OperationContextHolder.requireCurrent());
            assertTrue(SecurityContext.currentPrincipal().isEmpty());

            try (SecurityContextScope userScope =
                         SecurityContext.openScope(user)) {

                assertEquals(user, SecurityContext.currentPrincipal().orElseThrow());
                assertEquals(
                        OperationActorType.USER,
                        OperationContextHolder.requireCurrent().actor().type()
                );

                try (SecurityContextScope emptyScope =
                             SecurityContext.openScope(null)) {

                    assertTrue(SecurityContext.currentPrincipal().isEmpty());
                    assertTrue(OperationContextHolder.current().isEmpty());
                }

                assertEquals(user, SecurityContext.currentPrincipal().orElseThrow());
                assertEquals(
                        OperationActorType.USER,
                        OperationContextHolder.requireCurrent().actor().type()
                );
            }

            assertTrue(SecurityContext.currentPrincipal().isEmpty());
            assertEquals(jobContext, OperationContextHolder.requireCurrent());
        }
    }

    @Test
    void shouldRestoreThreeNestedSecurityScopes() {
        AuthenticatedUser outerUser = new AuthenticatedUser(
                "user-a",
                "outer-user",
                "tenant-a",
                Set.of("USER"),
                Set.of("outer:read")
        );

        AuthenticatedClient client = new AuthenticatedClient(
                "client-a",
                "outer-client",
                "tenant-a",
                Set.of("CLIENT"),
                Set.of("outer:read")
        );

        AuthenticatedUser innerUser = new AuthenticatedUser(
                "user-b",
                "inner-user",
                "tenant-a",
                Set.of("USER"),
                Set.of("inner:read")
        );

        try (SecurityContextScope outerScope =
                     SecurityContext.openScope(outerUser)) {

            assertEquals(
                    outerUser,
                    SecurityContext.currentPrincipal().orElseThrow()
            );

            try (SecurityContextScope clientScope =
                         SecurityContext.openScope(client)) {

                assertEquals(
                        client,
                        SecurityContext.currentPrincipal().orElseThrow()
                );

                try (SecurityContextScope innerScope =
                             SecurityContext.openScope(innerUser)) {

                    assertEquals(
                            innerUser,
                            SecurityContext.currentPrincipal().orElseThrow()
                    );
                }

                assertEquals(
                        client,
                        SecurityContext.currentPrincipal().orElseThrow()
                );
            }

            assertEquals(
                    outerUser,
                    SecurityContext.currentPrincipal().orElseThrow()
            );
        }

        assertTrue(SecurityContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }


    @Test
    void shouldRestoreContextsWhenScopedActionThrows() {
        OperationContext jobContext = jobContext();
        AuthenticatedUser user = new AuthenticatedUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );

        try (OperationContextScope jobScope =
                     OperationContextHolder.scope(jobContext)) {

            assertThrows(IllegalStateException.class, () -> {
                try (SecurityContextScope userScope =
                             SecurityContext.openScope(user)) {
                    throw new IllegalStateException("failed");
                }
            });

            assertTrue(SecurityContext.currentPrincipal().isEmpty());
            assertEquals(jobContext, OperationContextHolder.requireCurrent());
        }
    }

    @Test
    void shouldAllowSecurityScopeToBeClosedRepeatedly() {
        AuthenticatedUser user = new AuthenticatedUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of(),
                Set.of()
        );

        SecurityContextScope scope = SecurityContext.openScope(user);

        scope.close();
        scope.close();

        assertTrue(SecurityContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldNotCopyUserRolesAndPermissionsToOperationContext() {
        AuthenticatedUser user = new AuthenticatedUser(
                "user-1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of("system:user:list")
        );

        try (SecurityContextScope ignored =
                     SecurityContext.openScope(user)) {

            OperationContext operationContext =
                    OperationContextHolder.requireCurrent();

            assertTrue(operationContext.actor().attributes().isEmpty());
        }
    }

    private static OperationContext jobContext() {
        OperationActor actor = new OperationActor(OperationActorType.JOB, "job-1", "job-1" + "-name", "tenant-a", Map.of());
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
