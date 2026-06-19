package com.indigo.synapse.webmvc.context;

import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MvcOperationContextFilterTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldRestoreOperationContextDuringRequestAndClearAfterRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(OperationContextPropagationKeys.TRACE_ID, "trace-mvc");
        request.addHeader(OperationContextPropagationKeys.REQUEST_ID, "request-mvc");
        request.addHeader(OperationContextPropagationKeys.TENANT_ID, "tenant-1");
        request.addHeader(OperationContextPropagationKeys.ACTOR_TYPE, OperationActorType.USER.name());
        request.addHeader(OperationContextPropagationKeys.ACTOR_ID, "user-1");
        request.addHeader(OperationContextPropagationKeys.ACTOR_NAME, "admin");
        request.addHeader(OperationContextPropagationKeys.SOURCE_TYPE, "HTTP");
        request.addHeader(OperationContextPropagationKeys.SOURCE_NAME, "webmvc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MvcOperationContextFilter().doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("trace-mvc", OperationContextHolder.requireCurrent().traceId());
            assertEquals("request-mvc", OperationContextHolder.requireCurrent().requestId());
            assertEquals("tenant-1", OperationContextHolder.requireCurrent().tenantId());
            assertEquals("user-1", OperationContextHolder.requireCurrent().actor().id());
        });

        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldNotCreateContextWhenActorHeadersAreMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader(OperationContextPropagationKeys.TRACE_ID, "trace-only");
        request.addHeader(OperationContextPropagationKeys.TENANT_ID, "tenant-1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new MvcOperationContextFilter().doFilter(request, response, (servletRequest, servletResponse) ->
                assertTrue(OperationContextHolder.current().isEmpty()));

        assertTrue(OperationContextHolder.current().isEmpty());
    }
}
