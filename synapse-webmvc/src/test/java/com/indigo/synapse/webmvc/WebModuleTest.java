package com.indigo.synapse.webmvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WebModuleTest {

    @Test
    void shouldDependOnCommonModule() {
        assertEquals("synapse-core", WebModule.dependsOn());
    }
}
