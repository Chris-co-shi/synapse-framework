package com.indigo.synapse.security.permission;

import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPermissionCheckerTest {

    private final DefaultPermissionChecker permissionChecker = new DefaultPermissionChecker();

    @Test
    void shouldReturnTrueWhenCurrentUserHasPermission() {
        try (var ignored =
                     SecurityContext.openScope(user("system:user:create"))) {
            assertTrue(permissionChecker.has("system:user:create"));
        }
    }

    @Test
    void shouldReturnFalseWhenCurrentUserDoesNotHavePermission() {
        try (var ignored =
                     SecurityContext.openScope(user("system:user:list"))) {
            assertFalse(permissionChecker.has("system:user:create"));
        }
    }

    @Test
    void shouldReturnFalseWhenNoCurrentUser() {
        assertFalse(permissionChecker.has("system:user:create"));
    }

    @Test
    void shouldThrowAccessDeniedWhenPermissionMissing() {
        try (var ignored =
                     SecurityContext.openScope(user("system:user:list"))) {
            SynapseAccessDeniedException exception = assertThrows(
                    SynapseAccessDeniedException.class,
                    () -> permissionChecker.require("system:user:create")
            );
            assertEquals(
                    SecurityErrorCode.SECURITY_PERMISSION_DENIED,
                    exception.errorCode()
            );
        }
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUserMissing() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> permissionChecker.require("system:user:create")
        );

        assertEquals(SecurityErrorCode.SECURITY_UNAUTHENTICATED, exception.errorCode());
    }

    @Test
    void shouldRejectBlankPermissionForRequire() {
        assertThrows(IllegalArgumentException.class, () -> permissionChecker.require(null));
        assertThrows(IllegalArgumentException.class, () -> permissionChecker.require(""));
        assertThrows(IllegalArgumentException.class, () -> permissionChecker.require(" "));
    }

    @Test
    void shouldReturnCurrentUserWhenRequired() {
        AuthenticatedUser user = user("system:user:create");
        try (var ignored = SecurityContext.openScope(user)) {
            assertEquals(user, permissionChecker.requireUser());
        }
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenRequireUserWithoutCurrentUser() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                permissionChecker::requireUser
        );

        assertEquals(SecurityErrorCode.SECURITY_UNAUTHENTICATED, exception.errorCode());
    }

    private static AuthenticatedUser user(String permission) {
        return new AuthenticatedUser(
                "1",
                "admin",
                "tenant-a",
                Set.of("ADMIN"),
                Set.of(permission)
        );
    }
}