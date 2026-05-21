package com.indigo.synapse.web.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceIdResolverTest {

    @Test
    void shouldUseValidIncomingTraceId() {
        assertEquals("trace-1", TraceIdResolver.resolve(" trace-1 "));
    }

    @Test
    void shouldGenerateTraceIdWhenIncomingTraceIdIsMissingOrInvalid() {
        assertEquals(32, TraceIdResolver.resolve(null).length());
        assertEquals(32, TraceIdResolver.resolve(" ").length());
        assertEquals(32, TraceIdResolver.resolve("trace\r\nbad").length());
    }

    @Test
    void shouldValidateTraceIdCharacters() {
        assertTrue(TraceIdResolver.isValid("abc-ABC_123.456"));
        assertFalse(TraceIdResolver.isValid("abc/123"));
        assertFalse(TraceIdResolver.isValid("x".repeat(129)));
    }
}
