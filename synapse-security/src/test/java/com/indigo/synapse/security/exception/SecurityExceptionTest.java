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

        assertEquals("SECURITY_INVALID_TRUSTED_HEADER", SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER.code());
        assertEquals("非法可信请求头", SecurityErrorCode.SECURITY_INVALID_TRUSTED_HEADER.message());

        assertEquals("SECURITY_INVALID_SIGNATURE", SecurityErrorCode.SECURITY_INVALID_SIGNATURE.code());
        assertEquals("可信请求头签名无效", SecurityErrorCode.SECURITY_INVALID_SIGNATURE.message());

        assertEquals("SECURITY_TRUSTED_HEADER_EXPIRED", SecurityErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED.code());
        assertEquals("可信请求头已过期", SecurityErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED.message());

        assertEquals("SECURITY_PERMISSION_DENIED", SecurityErrorCode.SECURITY_PERMISSION_DENIED.code());
        assertEquals("无权限", SecurityErrorCode.SECURITY_PERMISSION_DENIED.message());
    }

    @Test
    void shouldCreateAuthenticationAndAccessDeniedExceptions() {
        SynapseAuthenticationException authenticationException = new SynapseAuthenticationException(
                SecurityErrorCode.SECURITY_INVALID_SIGNATURE,
                "invalid signature"
        );
        SynapseAccessDeniedException accessDeniedException = new SynapseAccessDeniedException(
                SecurityErrorCode.SECURITY_PERMISSION_DENIED,
                "permission denied"
        );

        assertEquals(SecurityErrorCode.SECURITY_INVALID_SIGNATURE, authenticationException.errorCode());
        assertEquals("invalid signature", authenticationException.getMessage());

        assertEquals(SecurityErrorCode.SECURITY_PERMISSION_DENIED, accessDeniedException.errorCode());
        assertEquals("permission denied", accessDeniedException.getMessage());
    }
}