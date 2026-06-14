package com.indigo.synapse.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.indigo.synapse.core.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

class SynapseExceptionTest {

    @Test
    void shouldKeepErrorCodeAndDefaultMessage() {
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_CONFLICT);

        assertSame(CommonErrorCode.COMMON_CONFLICT, exception.errorCode());
        assertEquals("数据冲突", exception.getMessage());
    }

    @Test
    void shouldAllowCustomMessage() {
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_BAD_REQUEST, "字段不能为空");

        assertSame(CommonErrorCode.COMMON_BAD_REQUEST, exception.errorCode());
        assertEquals("字段不能为空", exception.getMessage());
    }

    @Test
    void shouldKeepCauseWithDefaultMessage() {
        IllegalStateException cause = new IllegalStateException("origin");
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_INTERNAL_ERROR, cause);

        assertSame(CommonErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("系统内部错误", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldKeepCauseWithCustomMessage() {
        IllegalStateException cause = new IllegalStateException("origin");
        SynapseException exception = new SynapseException(CommonErrorCode.COMMON_INTERNAL_ERROR, "custom", cause);

        assertSame(CommonErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("custom", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldUseCommonUnauthorizedForDefaultAuthenticationException() {
        SynapseAuthenticationException exception = new SynapseAuthenticationException();

        assertSame(CommonErrorCode.COMMON_UNAUTHORIZED, exception.errorCode());
        assertEquals("未认证", exception.getMessage());
    }

    @Test
    void shouldUseCommonForbiddenForDefaultAccessDeniedException() {
        SynapseAccessDeniedException exception = new SynapseAccessDeniedException();

        assertSame(CommonErrorCode.COMMON_FORBIDDEN, exception.errorCode());
        assertEquals("无权限", exception.getMessage());
    }
}
