package com.indigo.synapse.security.permission;

import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.context.SecurityContext;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPermissionCheckerTest {

    private final DefaultPermissionChecker permissionChecker = new DefaultPermissionChecker();

    @AfterEach
    void tearDown() {
        SecurityContext.clear();
        OperationContextHolder.clear();
    }

    @Test
    void shouldReturnTrueWhenCurrentUserHasPermission() {
        SecurityContext.set(user("system:user:create"));

        assertTrue(permissionChecker.has("system:user:create"));
    }

    @Test
    void shouldReturnFalseWhenCurrentUserDoesNotHavePermission() {
        SecurityContext.set(user("system:user:list"));

        assertFalse(permissionChecker.has("system:user:create"));
    }

    @Test
    void shouldReturnFalseWhenNoCurrentUser() {
        assertFalse(permissionChecker.has("system:user:create"));
    }

    @Test
    void shouldReturnFalseForBlankPermission() {
        SecurityContext.set(user("system:user:create"));

        assertFalse(permissionChecker.has(null));
        assertFalse(permissionChecker.has(""));
        assertFalse(permissionChecker.has(" "));
    }

    @Test
    void shouldPassRequireWhenCurrentUserHasPermission() {
        SecurityContext.set(user("system:user:create"));

        assertDoesNotThrow(() -> permissionChecker.require("system:user:create"));
    }

    @Test
    void shouldThrowAccessDeniedWhenPermissionMissing() {
        SecurityContext.set(user("system:user:list"));

        SynapseAccessDeniedException exception = assertThrows(
                SynapseAccessDeniedException.class,
                () -> permissionChecker.require("system:user:create")
        );

        assertEquals(SecurityErrorCode.SECURITY_PERMISSION_DENIED, exception.errorCode());
        assertEquals(403, exception.errorCode().httpStatus());
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenUserMissing() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> permissionChecker.require("system:user:create")
        );

        assertEquals(SecurityErrorCode.SECURITY_UNAUTHENTICATED, exception.errorCode());
        assertEquals(401, exception.errorCode().httpStatus());
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
        SecurityContext.set(user);

        assertEquals(user, permissionChecker.requireUser());
    }

    @Test
    void shouldThrowAuthenticationExceptionWhenRequireUserWithoutCurrentUser() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                permissionChecker::requireUser
        );

        assertEquals(SecurityErrorCode.SECURITY_UNAUTHENTICATED, exception.errorCode());
        assertEquals(401, exception.errorCode().httpStatus());
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
