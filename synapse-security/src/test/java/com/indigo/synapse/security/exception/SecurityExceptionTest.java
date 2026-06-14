package com.indigo.synapse.security.exception;

import com.indigo.synapse.core.error.CommonErrorCode;
import org.junit.jupiter.api.Test;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import com.indigo.synapse.core.exception.SynapseAccessDeniedException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityExceptionTest {

    @Test
    void shouldExposeCommonErrorCodes() {
        assertEquals(401, CommonErrorCode.SECURITY_UNAUTHENTICATED.httpStatus());
        assertEquals(401, CommonErrorCode.SECURITY_INVALID_TRUSTED_HEADER.httpStatus());
        assertEquals(401, CommonErrorCode.SECURITY_INVALID_SIGNATURE.httpStatus());
        assertEquals(401, CommonErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED.httpStatus());
        assertEquals(403, CommonErrorCode.SECURITY_PERMISSION_DENIED.httpStatus());
    }

    @Test
    void shouldCreateAuthenticationAndAccessDeniedExceptions() {
        com.indigo.synapse.core.exception.SynapseAuthenticationException authenticationException = new com.indigo.synapse.core.exception.SynapseAuthenticationException(
                CommonErrorCode.SECURITY_INVALID_SIGNATURE,
                "invalid signature"
        );
        SynapseAccessDeniedException accessDeniedException = new SynapseAccessDeniedException("permission denied");

        assertEquals(CommonErrorCode.SECURITY_INVALID_SIGNATURE, authenticationException.errorCode());
        assertEquals("invalid signature", authenticationException.getMessage());
        assertEquals(CommonErrorCode.SECURITY_PERMISSION_DENIED, accessDeniedException.errorCode());
        assertEquals("permission denied", accessDeniedException.getMessage());
    }

    @Test
    void shouldRejectForbiddenCodeForAuthenticationException() {
        assertThrows(IllegalArgumentException.class, () ->
                new SynapseAuthenticationException(CommonErrorCode.SECURITY_PERMISSION_DENIED));
    }
}
