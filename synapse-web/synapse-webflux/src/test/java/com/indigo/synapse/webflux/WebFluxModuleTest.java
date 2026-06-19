package com.indigo.synapse.webflux;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebFluxModuleTest {

    @Test
    void shouldExposeModuleNameAndDependency() {
        assertEquals("synapse-webflux", WebFluxModule.NAME);
        assertEquals("synapse-web-core", WebFluxModule.dependsOn());
    }
}
