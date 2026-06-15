package com.indigo.synapse.cloud;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CloudModuleTest {

    @Test
    void shouldExposeModuleNameAndDependency() {
        assertEquals("synapse-cloud", CloudModule.NAME);
        assertEquals("synapse-core", CloudModule.dependsOn());
    }
}
