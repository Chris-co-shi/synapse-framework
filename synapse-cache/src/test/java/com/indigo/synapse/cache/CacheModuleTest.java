package com.indigo.synapse.cache;

import com.indigo.synapse.common.CommonModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheModuleTest {

    @Test
    void shouldExposeModuleNameAndDependency() {
        assertEquals("synapse-cache", CacheModule.NAME);
        assertEquals(CommonModule.NAME, CacheModule.dependsOn());
    }
}
