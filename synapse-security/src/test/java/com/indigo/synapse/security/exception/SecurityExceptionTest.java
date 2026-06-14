package com.indigo.synapse.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityExceptionTest {

    @Test
    void shouldExposeSecurityErrorCodes() {
        assertEquals(401, SecurityErrorCode.SECURITY_UNAUTHENTICATED.httpStatus());
        assertEquals(401, SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER.httpStatus());
        assertEquals(401, SecurityErrorCode.SECURITY_INVALID_SIGNATURE.httpStatus());
        assertEquals(401, SecurityErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED.httpStatus());
        assertEquals(403, SecurityErrorCode.SECURITY_PERMISSION_DENIED.httpStatus());
    }

    @Test
    void shouldCreateAuthenticationAndAccessDeniedExceptions() {
        SynapseAuthenticationException authenticationException = new SynapseAuthenticationException(
                SecurityErrorCode.SECURITY_INVALID_SIGNATURE,
                "invalid signature"
        );
        SynapseAccessDeniedException accessDeniedException = new SynapseAccessDeniedException("permission denied");

        assertEquals(SecurityErrorCode.SECURITY_INVALID_SIGNATURE, authenticationException.errorCode());
        assertEquals("invalid signature", authenticationException.getMessage());
        assertEquals(SecurityErrorCode.SECURITY_PERMISSION_DENIED, accessDeniedException.errorCode());
        assertEquals("permission denied", accessDeniedException.getMessage());
    }

    @Test
    void shouldRejectForbiddenCodeForAuthenticationException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SynapseAuthenticationException(SecurityErrorCode.SECURITY_PERMISSION_DENIED));
    }
}
