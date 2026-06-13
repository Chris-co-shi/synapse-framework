package com.indigo.synapse.datapermission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataPermissionModuleTest {

    @Test
    void shouldExposeModuleMetadata() {
        assertEquals("synapse-data-permission", DataPermissionModule.NAME);
        assertEquals("synapse-core", DataPermissionModule.dependsOn());
    }
}
