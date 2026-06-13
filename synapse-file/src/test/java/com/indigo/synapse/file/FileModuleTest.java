package com.indigo.synapse.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileModuleTest {

    @Test
    void shouldExposeModuleMetadata() {
        assertEquals("synapse-file", FileModule.NAME);
        assertEquals("synapse-core", FileModule.dependsOn());
    }
}
