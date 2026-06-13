package com.indigo.synapse.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskModuleTest {

    @Test
    void shouldExposeModuleMetadata() {
        assertEquals("synapse-task", TaskModule.NAME);
        assertEquals("synapse-core", TaskModule.dependsOn());
    }
}
