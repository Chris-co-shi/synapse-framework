package com.indigo.synapse.task.execution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScheduledTaskDefinitionTest {

    @Test
    void shouldCreateEnabledDefinition() {
        ScheduledTaskDefinition definition = ScheduledTaskDefinition.enabled("sync-user", "0 0 * * * *");

        assertTrue(definition.enabled());
    }

    @Test
    void shouldRejectInvalidDefinition() {
        assertThrows(IllegalArgumentException.class, () -> new ScheduledTaskDefinition(null, "0 0 * * * *", true));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledTaskDefinition("sync-user", " ", true));
    }
}
