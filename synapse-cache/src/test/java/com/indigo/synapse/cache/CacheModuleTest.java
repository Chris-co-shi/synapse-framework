package com.indigo.synapse.cache;

import com.indigo.synapse.common.CoreModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheModuleTest {

    @Test
    void shouldExposeModuleNameAndDependency() {
        assertEquals("synapse-cache", CacheModule.NAME);
        assertEquals(CoreModule.NAME, CacheModule.dependsOn());
    }
}
