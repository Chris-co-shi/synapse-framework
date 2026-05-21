package com.indigo.synapse.example;

import com.indigo.synapse.starter.StarterModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExampleModuleTest {

    @Test
    void shouldDependOnStarterModule() {
        assertEquals(StarterModule.NAME, ExampleModule.dependsOn());
    }
}
