package com.indigo.synapse.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SecurityModuleTest {

    @Test
    void shouldDependOnCommonModule() {
        assertEquals("synapse-common", SecurityModule.dependsOn());
    }
}
