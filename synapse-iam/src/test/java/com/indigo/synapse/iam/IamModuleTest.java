package com.indigo.synapse.iam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IamModuleTest {

    @Test
    void shouldExposeModuleName() {
        assertEquals("synapse-iam", IamModule.name());
    }
}
