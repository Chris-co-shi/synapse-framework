package com.indigo.synapse.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageModuleTest {

    @Test
    void shouldExposeModuleMetadata() {
        assertEquals("synapse-message", MessageModule.NAME);
        assertEquals("synapse-core", MessageModule.dependsOn());
    }
}
