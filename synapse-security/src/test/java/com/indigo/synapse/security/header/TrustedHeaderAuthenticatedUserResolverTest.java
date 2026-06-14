package com.indigo.synapse.security.header;

import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.security.exception.SecurityErrorCode;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustedHeaderAuthenticatedUserResolverTest {

    private final TrustedHeaderAuthenticatedUserResolver resolver = new TrustedHeaderAuthenticatedUserResolver();

    @Test
    void shouldResolvePrincipalWithTrimmedRolesAndPermissions() {
        TrustedHeaderPrincipal principal = resolver.resolvePrincipal(headers(Map.of(
                SecurityHeaders.ROLES, " ADMIN, , USER ",
                SecurityHeaders.PERMISSIONS, " system:user:list, ,system:user:create ",
                SecurityHeaders.TENANT_ID, " tenant-a ",
                SecurityHeaders.TRACE_ID, " trace-1 ",
                SecurityHeaders.REQUEST_ID, " request-1 ",
                SecurityHeaders.SOURCE, " gateway ",
                SecurityHeaders.TIMESTAMP, " 1780000000000 ",
                SecurityHeaders.NONCE, " nonce-1 "
        )));

        assertEquals("1", principal.userId());
        assertEquals("admin", principal.username());
        assertEquals("tenant-a", principal.tenantId());
        assertEquals(Set.of("ADMIN", "USER"), principal.roles());
        assertEquals(Set.of("system:user:list", "system:user:create"), principal.permissions());
        assertEquals("trace-1", principal.traceId());
        assertEquals("request-1", principal.requestId());
        assertEquals("gateway", principal.source());
        assertEquals("1780000000000", principal.timestamp());
        assertEquals("nonce-1", principal.nonce());
    }

    @Test
    void shouldResolveAuthenticatedUserWithoutForgingRolesOrPermissions() {
        AuthenticatedUser authenticatedUser = resolver.resolveAuthenticatedUser(headers(Map.of()));

        assertEquals("1", authenticatedUser.userId());
        assertEquals("admin", authenticatedUser.username());
        assertTrue(authenticatedUser.roles().isEmpty());
        assertTrue(authenticatedUser.permissions().isEmpty());
    }

    @Test
    void shouldExposeImmutableRoleAndPermissionSets() {
        TrustedHeaderPrincipal principal = resolver.resolvePrincipal(headers(Map.of(
                SecurityHeaders.ROLES, "ADMIN",
                SecurityHeaders.PERMISSIONS, "system:user:list"
        )));

        assertThrows(UnsupportedOperationException.class, () -> principal.roles().add("USER"));
        assertThrows(UnsupportedOperationException.class, () -> principal.permissions().add("system:user:create"));
    }

    @Test
    void shouldRejectMissingRequiredHeaders() {
        SynapseAuthenticationException missingUserId = assertThrows(
                SynapseAuthenticationException.class,
                () -> resolver.resolvePrincipal(Map.of(SecurityHeaders.USERNAME, "admin"))
        );
        SynapseAuthenticationException missingUsername = assertThrows(
                SynapseAuthenticationException.class,
                () -> resolver.resolvePrincipal(Map.of(SecurityHeaders.USER_ID, "1"))
        );

        assertEquals(SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER, missingUserId.errorCode());
        assertEquals(SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER, missingUsername.errorCode());
    }

    private static Map<String, String> headers(Map<String, String> overrides) {
        Map<String, String> headers = new HashMap<>();
        headers.put(SecurityHeaders.USER_ID, " 1 ");
        headers.put(SecurityHeaders.USERNAME, " admin ");
        headers.putAll(overrides);
        return headers;
    }
}
