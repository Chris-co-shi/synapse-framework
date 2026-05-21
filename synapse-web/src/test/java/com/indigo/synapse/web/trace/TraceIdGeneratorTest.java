package com.indigo.synapse.web.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TraceIdGeneratorTest {

    @Test
    void shouldGenerateCompactUuidTraceId() {
        String first = TraceIdGenerator.generate();
        String second = TraceIdGenerator.generate();

        assertEquals(32, first.length());
        assertNotEquals(first, second);
    }
}
