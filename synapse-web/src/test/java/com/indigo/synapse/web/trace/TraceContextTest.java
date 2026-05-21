package com.indigo.synapse.web.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceContextTest {

    @AfterEach
    void tearDown() {
        TraceContext.clear();
    }

    @Test
    void shouldKeepTraceIdInCurrentThread() {
        TraceContext.setTraceId("trace-1");

        assertTrue(TraceContext.currentTraceId().isPresent());
    }

    @Test
    void blankTraceIdShouldClearContext() {
        TraceContext.setTraceId("trace-1");
        TraceContext.setTraceId(" ");

        assertFalse(TraceContext.currentTraceId().isPresent());
    }
}
