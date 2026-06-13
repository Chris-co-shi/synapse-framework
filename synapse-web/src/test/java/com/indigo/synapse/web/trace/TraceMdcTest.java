package com.indigo.synapse.web.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceMdcTest {

    @AfterEach
    void tearDown() {
        TraceMdc.clear();
    }

    @Test
    void shouldKeepTraceIdInMdc() {
        TraceMdc.setTraceId("trace-1");

        assertEquals("trace-1", TraceMdc.currentTraceId().orElseThrow());
    }

    @Test
    void blankTraceIdShouldClearMdc() {
        TraceMdc.setTraceId("trace-1");
        TraceMdc.setTraceId(" ");

        assertTrue(TraceMdc.currentTraceId().isEmpty());
    }
}
