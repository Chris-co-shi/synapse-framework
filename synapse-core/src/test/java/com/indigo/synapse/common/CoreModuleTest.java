package com.indigo.synapse.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.indigo.synapse.core.CoreModule;
import org.junit.jupiter.api.Test;

class CoreModuleTest {

    @Test
    void shouldExposeModuleName() {
        assertEquals("synapse-core", CoreModule.NAME);
    }
}
