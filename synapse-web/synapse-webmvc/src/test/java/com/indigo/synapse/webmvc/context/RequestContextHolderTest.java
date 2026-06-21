package com.indigo.synapse.webmvc.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestContextHolderTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldStoreAndClearRequestContext() {
        RequestContext context = new RequestContext(
                "trace-1", "request-1", "GET", "/api/admin/users", "127.0.0.1");

        RequestContextHolder.set(context);

        assertEquals(context, RequestContextHolder.current().orElseThrow());

        RequestContextHolder.clear();

        assertTrue(RequestContextHolder.current().isEmpty());
    }

    @Test
    void shouldValidateRequiredContextFields() {
        assertThrows(IllegalArgumentException.class,
                () -> new RequestContext("", "request", "GET", "/x", null));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestContext("trace", "", "GET", "/x", null));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestContext("trace", "request", "", "/x", null));
        assertThrows(IllegalArgumentException.class,
                () -> new RequestContext("trace", "request", "GET", "", null));
    }
}
