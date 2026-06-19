package com.indigo.synapse.web.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebCoreModuleTest {

    @Test
    void shouldDependOnlyOnCoreModule() {
        assertEquals("synapse-core", WebCoreModule.dependsOn());
    }
}
