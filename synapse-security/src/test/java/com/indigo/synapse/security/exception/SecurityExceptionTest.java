package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityExceptionTest {

    @Test
    void shouldExposeSecurityErrorCodes() {
        assertEquals("SECURITY_UNAUTHENTICATED", SecurityErrorCode.SECURITY_UNAUTHENTICATED.code());
        assertEquals("未认证", SecurityErrorCode.SECURITY_UNAUTHENTICATED.message());

        assertEquals("SECURITY_PERMISSION_DENIED", SecurityErrorCode.SECURITY_PERMISSION_DENIED.code());
        assertEquals("无权限", SecurityErrorCode.SECURITY_PERMISSION_DENIED.message());
    }

    @Test
    void shouldCreateAuthenticationAndAccessDeniedExceptions() {
        SynapseAuthenticationException authenticationException = new SynapseAuthenticationException(
                SecurityErrorCode.SECURITY_UNAUTHENTICATED,
                "unauthenticated"
        );
        SynapseAccessDeniedException accessDeniedException = new SynapseAccessDeniedException(
                SecurityErrorCode.SECURITY_PERMISSION_DENIED,
                "permission denied"
        );

        assertEquals(SecurityErrorCode.SECURITY_UNAUTHENTICATED, authenticationException.errorCode());
        assertEquals("unauthenticated", authenticationException.getMessage());

        assertEquals(SecurityErrorCode.SECURITY_PERMISSION_DENIED, accessDeniedException.errorCode());
        assertEquals("permission denied", accessDeniedException.getMessage());
    }
}
