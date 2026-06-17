package com.indigo.synapse.webflux.response;

import com.indigo.synapse.core.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTest {

    @Test
    void successShouldUseStableSuccessCode() {
        Result<Void> response = Result.success(null, "trace-success");

        assertEquals("0", response.code());
        assertEquals("success", response.message());
        assertNull(response.data());
        assertEquals("trace-success", response.traceId());
        assertNotNull(response.timestamp());
    }

    @Test
    void successShouldKeepDataAndExplicitTraceId() {
        Result<String> response = Result.success("ok", "trace-data");

        assertEquals("0", response.code());
        assertEquals("success", response.message());
        assertEquals("ok", response.data());
        assertEquals("trace-data", response.traceId());
    }

    @Test
    void failShouldUseErrorCodeDefaultMessageAndNullData() {
        Result<Void> response = Result.fail(CommonErrorCode.COMMON_NOT_FOUND, null, "trace-fail");

        assertEquals("COMMON_NOT_FOUND", response.code());
        assertEquals("资源不存在", response.message());
        assertNull(response.data());
        assertEquals("trace-fail", response.traceId());
        assertNotNull(response.timestamp());
    }

    @Test
    void failShouldAllowCustomMessageAndExplicitTraceId() {
        Result<Void> response = Result.fail(CommonErrorCode.COMMON_BAD_REQUEST, "参数 id 不能为空", "trace-custom");

        assertEquals("COMMON_BAD_REQUEST", response.code());
        assertEquals("参数 id 不能为空", response.message());
        assertNull(response.data());
        assertEquals("trace-custom", response.traceId());
    }

    @Test
    void shouldGenerateTraceIdWhenNoTraceIdExists() {
        Result<Void> response = Result.success();

        assertNotNull(response.traceId());
        assertEquals(32, response.traceId().length());
    }
}
