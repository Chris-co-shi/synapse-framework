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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityContextTest {

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldStoreAndClearCurrentUser() {
        AuthenticatedUser user = user("1", "admin");

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
        SecurityContext.set(user("1", "admin"));

        SecurityContext.set(null);

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldLeaveIndependentOperationContextUntouchedWhenRepeatedlyCleared() {
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");
        OperationContextHolder.set(jobContext);

        SecurityContext.clear();
        SecurityContext.clearIfEmpty();
        SecurityContext.clear();

        assertSame(jobContext, OperationContextHolder.requireCurrent());
    }

    @Test
    void shouldRestorePreviousOperationContextWhenCleared() {
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");

        try (OperationContextScope ignored = OperationContextHolder.scope(jobContext)) {
            SecurityContext.set(user("1", "admin"));

            assertEquals(OperationActorType.USER, OperationContextHolder.requireCurrent().actor().type());

            SecurityContext.clear();

            assertSame(jobContext, OperationContextHolder.requireCurrent());
        }
    }

    @Test
    void shouldReplaceCurrentUserWithoutRestoringReplacedUserOnClear() {
        OperationContext jobContext = context(OperationActorType.JOB, "job-1");

        try (OperationContextScope ignored = OperationContextHolder.scope(jobContext)) {
            SecurityContext.set(user("1", "first"));
            SecurityContext.set(user("2", "second"));

            assertEquals("2", SecurityContext.currentUser().orElseThrow().userId());
            assertEquals("2", OperationContextHolder.requireCurrent().actor().id());

            SecurityContext.clear();

            assertTrue(SecurityContext.currentUser().isEmpty());
            assertSame(jobContext, OperationContextHolder.requireCurrent());
        }
    }

    @Test
    void shouldRestoreOuterSecurityAndOperationContextAfterNestedScope() {
        AuthenticatedUser outerUser = user("1", "outer");
        AuthenticatedUser innerUser = user("2", "inner");
        SecurityContext.set(outerUser);
        OperationContext outerOperationContext = OperationContextHolder.requireCurrent();

        try (SecurityContextScope ignored = SecurityContext.scope(innerUser)) {
            assertEquals(innerUser, SecurityContext.currentUser().orElseThrow());
            assertEquals("2", OperationContextHolder.requireCurrent().actor().id());
        }

        assertEquals(outerUser, SecurityContext.currentUser().orElseThrow());
        assertSame(outerOperationContext, OperationContextHolder.requireCurrent());
    }

    @Test
    void shouldRestoreOuterScopeWhenNestedActionThrows() {
        AuthenticatedUser outerUser = user("1", "outer");
        SecurityContext.set(outerUser);
        OperationContext outerOperationContext = OperationContextHolder.requireCurrent();

        assertThrows(IllegalStateException.class, () -> {
            try (SecurityContextScope ignored = SecurityContext.scope(user("2", "inner"))) {
                throw new IllegalStateException("failed");
            }
        });

        assertEquals(outerUser, SecurityContext.currentUser().orElseThrow());
        assertSame(outerOperationContext, OperationContextHolder.requireCurrent());
    }

    @Test
    void shouldTemporarilyClearSecurityUserAndRestoreItAfterScope() {
        AuthenticatedUser outerUser = user("1", "outer");
        SecurityContext.set(outerUser);
        OperationContext outerOperationContext = OperationContextHolder.requireCurrent();

        try (SecurityContextScope ignored = SecurityContext.scope(null)) {
            assertTrue(SecurityContext.currentUser().isEmpty());
            assertTrue(OperationContextHolder.current().isEmpty());
        }

        assertEquals(outerUser, SecurityContext.currentUser().orElseThrow());
        assertSame(outerOperationContext, OperationContextHolder.requireCurrent());
    }

    @Test
    void shouldAllowRepeatedScopeClose() {
        SecurityContextScope scope = SecurityContext.scope(user("1", "admin"));

        scope.close();
        scope.close();

        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldRejectClosingScopeFromAnotherThread() throws InterruptedException {
        SecurityContextScope scope = SecurityContext.scope(user("1", "admin"));
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                scope.close();
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });

        thread.start();
        thread.join();

        assertTrue(failure.get() instanceof IllegalStateException);
        assertEquals("1", SecurityContext.currentUser().orElseThrow().userId());

        scope.close();
        assertTrue(SecurityContext.currentUser().isEmpty());
    }

    @Test
    void shouldNotLeakBetweenSequentialScopesOnReusedThread() {
        try (SecurityContextScope ignored = SecurityContext.scope(user("1", "first"))) {
            assertEquals("1", SecurityContext.currentUser().orElseThrow().userId());
        }
        assertTrue(SecurityContext.currentUser().isEmpty());

        try (SecurityContextScope ignored = SecurityContext.scope(user("2", "second"))) {
            assertEquals("2", SecurityContext.currentUser().orElseThrow().userId());
        }
        assertTrue(SecurityContext.currentUser().isEmpty());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    private static AuthenticatedUser user(String userId, String username) {
        return new AuthenticatedUser(userId, username, "tenant-a", Set.of("ADMIN"), Set.of("system:user:list"));
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
