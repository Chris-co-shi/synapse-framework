package com.indigo.synapse.starter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class StarterModuleTest {

    @Test
    void shouldAggregateFrameworkModules() {
        assertEquals(
                List.of("synapse-common", "synapse-web", "synapse-data", "synapse-security", "synapse-audit"),
                StarterModule.modules()
        );
    }
}
