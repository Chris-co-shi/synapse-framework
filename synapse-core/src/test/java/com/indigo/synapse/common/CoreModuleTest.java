package com.indigo.synapse.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CoreModuleTest {

    @Test
    void shouldExposeModuleName() {
        assertEquals("synapse-core", CoreModule.NAME);
    }
}
