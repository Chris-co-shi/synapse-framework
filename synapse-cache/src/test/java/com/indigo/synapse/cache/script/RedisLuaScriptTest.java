package com.indigo.synapse.cache.script;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisLuaScriptTest {

    @Test
    void shouldExposeScriptMetadata() {
        assertEquals("synapse-reentrant-lock", SynapseRedisScripts.REENTRANT_LOCK.name());
        assertEquals(Long.class, SynapseRedisScripts.REENTRANT_LOCK.resultType());
        assertFalse(SynapseRedisScripts.REENTRANT_LOCK.source().isBlank());
    }

    @Test
    void shouldRejectInvalidScript() {
        assertThrows(IllegalArgumentException.class, () -> new RedisLuaScript<>("", "return 1", Long.class));
        assertThrows(IllegalArgumentException.class, () -> new RedisLuaScript<>("x", "", Long.class));
        assertThrows(IllegalArgumentException.class, () -> new RedisLuaScript<>("x", "return 1", null));
    }
}
