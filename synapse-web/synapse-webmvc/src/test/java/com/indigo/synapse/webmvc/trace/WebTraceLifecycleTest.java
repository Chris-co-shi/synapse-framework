package com.indigo.synapse.webmvc.trace;

import com.indigo.synapse.webmvc.context.RequestContext;
import com.indigo.synapse.webmvc.context.RequestContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebTraceLifecycleTest {

    @AfterEach
    void tearDown() {
        WebTraceLifecycle.end();
    }

    @Test
    void shouldStartAndEndTraceLifecycle() {
        RequestContext context = WebTraceLifecycle.start(
                "trace-1", "request-1", "GET", "/api/admin/users", "127.0.0.1");

        assertEquals("trace-1", context.traceId());
        assertEquals("trace-1", TraceContext.currentTraceId().orElseThrow());
        assertEquals("trace-1", TraceMdc.currentTraceId().orElseThrow());
        assertEquals(context, RequestContextHolder.current().orElseThrow());

        WebTraceLifecycle.end();

        assertTrue(TraceContext.currentTraceId().isEmpty());
        assertTrue(TraceMdc.currentTraceId().isEmpty());
        assertTrue(RequestContextHolder.current().isEmpty());
    }
}
