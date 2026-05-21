package com.indigo.synapse.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CommonErrorCodeTest {

    @Test
    void successCodeShouldBeZero() {
        assertEquals("0", CommonErrorCode.SUCCESS.code());
        assertEquals("success", CommonErrorCode.SUCCESS.message());
        assertEquals(200, CommonErrorCode.SUCCESS.httpStatus());
    }

    @Test
    void failureCodeShouldNotUseSuccessCode() {
        assertNotEquals(CommonErrorCode.SUCCESS.code(), CommonErrorCode.COMMON_BAD_REQUEST.code());
        assertEquals("请求参数错误", CommonErrorCode.COMMON_BAD_REQUEST.message());
        assertEquals(400, CommonErrorCode.COMMON_BAD_REQUEST.httpStatus());
    }
}
