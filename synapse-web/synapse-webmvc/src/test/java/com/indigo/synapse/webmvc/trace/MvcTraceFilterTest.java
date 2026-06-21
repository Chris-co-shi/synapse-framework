package com.indigo.synapse.webmvc.trace;

import com.indigo.synapse.web.core.trace.TraceHeaders;
import com.indigo.synapse.webmvc.context.RequestContextHolder;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MvcTraceFilterTest {

    @AfterEach
    void tearDown() {
        WebTraceLifecycle.end();
    }

    @Test
    void shouldSetTraceHeaderAndClearContext() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        request.addHeader(TraceHeaders.TRACE_ID, "trace-mvc");
        request.addHeader(TraceHeaders.REQUEST_ID, "request-mvc");
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MvcTraceFilter().doFilter(request, response, new MockFilterChain());

        assertEquals("trace-mvc", response.getHeader(TraceHeaders.TRACE_ID));
        assertEquals("request-mvc", response.getHeader(TraceHeaders.REQUEST_ID));
        assertTrue(TraceContext.currentTraceId().isEmpty());
        assertTrue(TraceMdc.currentTraceId().isEmpty());
        assertTrue(RequestContextHolder.current().isEmpty());
    }
}
