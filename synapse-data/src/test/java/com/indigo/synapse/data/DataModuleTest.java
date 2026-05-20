package com.indigo.synapse.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DataModuleTest {

    @Test
    void shouldDependOnCommonModule() {
        assertEquals("synapse-common", DataModule.dependsOn());
    }
}
