package com.indigo.synapse.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommonModuleTest {

    @Test
    void shouldExposeModuleName() {
        assertEquals("synapse-common", CommonModule.NAME);
    }
}
