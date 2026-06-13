package com.indigo.synapse.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WebModuleTest {

    @Test
    void shouldDependOnCommonModule() {
        assertEquals("synapse-core", WebModule.dependsOn());
    }
}
