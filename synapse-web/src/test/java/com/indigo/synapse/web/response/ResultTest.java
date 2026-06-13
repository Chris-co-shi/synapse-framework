package com.indigo.synapse.web.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.web.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ResultTest {

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void successShouldUseStableSuccessCode() {
        TraceContext.setTraceId("trace-success");

        Result<Void> response = Result.success();

        assertEquals("0", response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
        assertEquals("trace-success", response.getTraceId());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void successShouldKeepData() {
        Result<String> response = Result.success("ok");

        assertEquals("0", response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("ok", response.getData());
    }

    @Test
    void failShouldUseErrorCodeDefaultMessageAndNullData() {
        TraceContext.setTraceId("trace-fail");

        Result<Void> response = Result.fail(CommonErrorCode.COMMON_NOT_FOUND);

        assertEquals("COMMON_NOT_FOUND", response.getCode());
        assertEquals("资源不存在", response.getMessage());
        assertNull(response.getData());
        assertEquals("trace-fail", response.getTraceId());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void failShouldAllowCustomMessage() {
        Result<Void> response = Result.fail(CommonErrorCode.COMMON_BAD_REQUEST, "参数 id 不能为空");

        assertEquals("COMMON_BAD_REQUEST", response.getCode());
        assertEquals("参数 id 不能为空", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void shouldGenerateTraceIdWhenNoTraceContextExists() {
        Result<Void> response = Result.success();

        assertNotNull(response.getTraceId());
        assertEquals(32, response.getTraceId().length());
    }
}
