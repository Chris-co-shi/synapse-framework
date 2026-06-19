package com.indigo.synapse.web.core.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.indigo.synapse.core.error.CommonErrorCode;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void successShouldUseStableSuccessCodeAndExplicitTraceId() {
        Result<Void> response = Result.success(null, "trace-success");

        assertEquals("0", response.code());
        assertEquals("success", response.message());
        assertNull(response.data());
        assertEquals("trace-success", response.traceId());
        assertNotNull(response.timestamp());
    }

    @Test
    void successShouldKeepData() {
        Result<String> response = Result.success("ok");

        assertEquals("0", response.code());
        assertEquals("success", response.message());
        assertEquals("ok", response.data());
    }

    @Test
    void failShouldUseErrorCodeDefaultMessageAndNullData() {
        Result<Void> response = Result.fail(
                CommonErrorCode.COMMON_NOT_FOUND,
                CommonErrorCode.COMMON_NOT_FOUND.message(),
                "trace-fail"
        );

        assertEquals("COMMON_NOT_FOUND", response.code());
        assertEquals("资源不存在", response.message());
        assertNull(response.data());
        assertEquals("trace-fail", response.traceId());
        assertNotNull(response.timestamp());
    }

    @Test
    void failShouldAllowCustomMessage() {
        Result<Void> response = Result.fail(CommonErrorCode.COMMON_BAD_REQUEST, "参数 id 不能为空");

        assertEquals("COMMON_BAD_REQUEST", response.code());
        assertEquals("参数 id 不能为空", response.message());
        assertNull(response.data());
    }

    @Test
    void shouldGenerateTraceIdWhenNoTraceContextExists() {
        Result<Void> response = Result.success();

        assertNotNull(response.traceId());
        assertEquals(32, response.traceId().length());
    }
}
