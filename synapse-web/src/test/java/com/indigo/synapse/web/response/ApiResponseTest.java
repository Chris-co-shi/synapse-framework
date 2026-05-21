package com.indigo.synapse.web.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.indigo.synapse.common.error.CommonErrorCode;
import com.indigo.synapse.web.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void successShouldUseStableSuccessCode() {
        TraceContext.setTraceId("trace-success");

        ApiResponse<Void> response = ApiResponse.success();

        assertEquals("0", response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
        assertEquals("trace-success", response.getTraceId());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void successShouldKeepData() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals("0", response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("ok", response.getData());
    }

    @Test
    void failShouldUseErrorCodeDefaultMessageAndNullData() {
        TraceContext.setTraceId("trace-fail");

        ApiResponse<Void> response = ApiResponse.fail(CommonErrorCode.COMMON_NOT_FOUND);

        assertEquals("COMMON_NOT_FOUND", response.getCode());
        assertEquals("资源不存在", response.getMessage());
        assertNull(response.getData());
        assertEquals("trace-fail", response.getTraceId());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void failShouldAllowCustomMessage() {
        ApiResponse<Void> response = ApiResponse.fail(CommonErrorCode.COMMON_BAD_REQUEST, "参数 id 不能为空");

        assertEquals("COMMON_BAD_REQUEST", response.getCode());
        assertEquals("参数 id 不能为空", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void shouldGenerateTraceIdWhenNoTraceContextExists() {
        ApiResponse<Void> response = ApiResponse.success();

        assertNotNull(response.getTraceId());
        assertEquals(32, response.getTraceId().length());
    }
}
