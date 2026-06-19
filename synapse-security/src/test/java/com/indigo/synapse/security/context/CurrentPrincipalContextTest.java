package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import com.indigo.synapse.security.context.internal.PrincipalContextBinder;
import com.indigo.synapse.security.context.internal.PrincipalContextScope;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CurrentPrincipalContextTest {

    @Test
    void shouldStoreClientWithoutMappingToUser() {
        AuthenticatedClient client = new AuthenticatedClient(
                "client-a",
                "message-service",
                "tenant-a",
                Set.of("INTERNAL"),
                Set.of("message:send")
        );

        try (PrincipalContextScope ignored = PrincipalContextBinder.bind(client)) {
            assertEquals(client, CurrentPrincipalContext.currentPrincipal().orElseThrow());
            assertEquals(client, CurrentPrincipalContext.currentClient().orElseThrow());
            assertTrue(CurrentPrincipalContext.currentUser().isEmpty());

            OperationContext operationContext = OperationContextHolder.current().orElseThrow();
            assertEquals(OperationActorType.SERVICE, operationContext.actor().type());
            assertEquals("client-a", operationContext.actor().id());
            assertEquals("message-service", operationContext.actor().name());
            assertTrue(operationContext.actor().attributes().isEmpty());
        }

        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRestoreNestedSecurityAndOperationContextScopes() {
        AuthenticatedUser user = new AuthenticatedUser("1", "admin", "tenant-a", Set.of(), Set.of("a"));
        AuthenticatedClient client = new AuthenticatedClient("client-a", "client-a", "tenant-b", Set.of(), Set.of("b"));

        try (PrincipalContextScope userScope = PrincipalContextBinder.bind(user)) {
            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());
            assertEquals(user, CurrentPrincipalContext.currentUser().orElseThrow());

            try (PrincipalContextScope clientScope = PrincipalContextBinder.bind(client)) {
                assertEquals(OperationActorType.SERVICE, OperationContextHolder.requireCurrent().actor().type());
                assertEquals(client, CurrentPrincipalContext.currentClient().orElseThrow());
                assertTrue(CurrentPrincipalContext.currentUser().isEmpty());
            }

            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());
            assertEquals(user, CurrentPrincipalContext.currentUser().orElseThrow());
        }

        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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
            assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());

            try (PrincipalContextScope userScope =
                         PrincipalContextBinder.bind(user)) {

                assertEquals(user, CurrentPrincipalContext.currentPrincipal().orElseThrow());
                assertEquals(
                        OperationActorType.USER,
                        OperationContextHolder.requireCurrent().actor().type()
                );

                try (PrincipalContextScope emptyScope =
                             PrincipalContextBinder.bind(null)) {

                    assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
                    assertTrue(OperationContextHolder.current().isEmpty());
                }

                assertEquals(user, CurrentPrincipalContext.currentPrincipal().orElseThrow());
                assertEquals(
                        OperationActorType.USER,
                        OperationContextHolder.requireCurrent().actor().type()
                );
            }

            assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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

        try (PrincipalContextScope outerScope =
                     PrincipalContextBinder.bind(outerUser)) {

            assertEquals(
                    outerUser,
                    CurrentPrincipalContext.currentPrincipal().orElseThrow()
            );

            try (PrincipalContextScope clientScope =
                         PrincipalContextBinder.bind(client)) {

                assertEquals(
                        client,
                        CurrentPrincipalContext.currentPrincipal().orElseThrow()
                );

                try (PrincipalContextScope innerScope =
                             PrincipalContextBinder.bind(innerUser)) {

                    assertEquals(
                            innerUser,
                            CurrentPrincipalContext.currentPrincipal().orElseThrow()
                    );
                }

                assertEquals(
                        client,
                        CurrentPrincipalContext.currentPrincipal().orElseThrow()
                );
            }

            assertEquals(
                    outerUser,
                    CurrentPrincipalContext.currentPrincipal().orElseThrow()
            );
        }

        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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
                try (PrincipalContextScope userScope =
                             PrincipalContextBinder.bind(user)) {
                    throw new IllegalStateException("failed");
                }
            });

            assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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

        PrincipalContextScope scope = PrincipalContextBinder.bind(user);

        scope.close();
        scope.close();

        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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

        try (PrincipalContextScope ignored =
                     PrincipalContextBinder.bind(user)) {

            OperationContext operationContext =
                    OperationContextHolder.requireCurrent();

            assertTrue(operationContext.actor().attributes().isEmpty());
        }
    }

    @Test
    void shouldExposeReadOnlyPublicApi() {
        Set<String> publicStaticMethods =
                Arrays.stream(CurrentPrincipalContext.class.getDeclaredMethods())
                        .filter(method -> Modifier.isPublic(method.getModifiers()))
                        .filter(method -> Modifier.isStatic(method.getModifiers()))
                        .map(Method::getName)
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(
                Set.of(
                        "currentPrincipal",
                        "currentUser",
                        "currentClient"
                ),
                publicStaticMethods
        );
    }

    @Test
    void shouldIsolatePrincipalsAcrossConcurrentThreads() throws Exception {
        AuthenticatedUser first = new AuthenticatedUser(
                "user-a", "first", "tenant-a", Set.of(), Set.of()
        );
        AuthenticatedUser second = new AuthenticatedUser(
                "user-b", "second", "tenant-b", Set.of(), Set.of()
        );
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<String> firstResult = executor.submit(
                    () -> readPrincipalWhileConcurrent(first, barrier)
            );
            Future<String> secondResult = executor.submit(
                    () -> readPrincipalWhileConcurrent(second, barrier)
            );

            assertEquals("user-a", firstResult.get());
            assertEquals("user-b", secondResult.get());
        } finally {
            executor.shutdownNow();
        }

        assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
    }

    private static String readPrincipalWhileConcurrent(
            AuthenticatedUser principal,
            CyclicBarrier barrier
    ) throws Exception {
        try (PrincipalContextScope ignored = PrincipalContextBinder.bind(principal)) {
            barrier.await();
            return CurrentPrincipalContext.currentPrincipal()
                    .orElseThrow()
                    .principalId();
        } finally {
            assertTrue(CurrentPrincipalContext.currentPrincipal().isEmpty());
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
