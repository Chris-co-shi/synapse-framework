package com.indigo.synapse.common.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.indigo.synapse.common.error.CommonErrorCode;
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
}
