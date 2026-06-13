package com.indigo.synapse.common.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class UuidIdGeneratorTest {

    @Test
    void shouldGenerate32CharIdWithoutDash() {
        String id = UuidIdGenerator.INSTANCE.generate();

        assertNotNull(id);
        assertEquals(32, id.length());
        assertFalse(id.contains("-"));
    }
}
