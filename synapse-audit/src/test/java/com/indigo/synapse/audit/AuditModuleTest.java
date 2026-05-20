package com.indigo.synapse.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AuditModuleTest {

    @Test
    void shouldDependOnCommonModule() {
        assertEquals("synapse-common", AuditModule.dependsOn());
    }
}
