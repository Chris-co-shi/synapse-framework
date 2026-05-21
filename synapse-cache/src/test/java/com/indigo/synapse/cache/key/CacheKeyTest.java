package com.indigo.synapse.cache.key;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheKeyTest {

    @Test
    void shouldBuildNamespacedCacheKey() {
        CacheKey key = CacheKey.of("security", "token", "denylist", "tenant-a", "jti-1");

        assertEquals("synapse:security:token:denylist:tenant-a:jti-1", key.value());
        assertEquals(key.value(), key.toString());
    }

    @Test
    void shouldRejectBlankSegment() {
        assertThrows(IllegalArgumentException.class, () -> CacheKey.of("cache", " ", "lock"));
    }

    @Test
    void shouldRejectSeparatorInSegment() {
        assertThrows(IllegalArgumentException.class, () -> CacheKey.of("cache", "order:payment", "lock"));
    }

    @Test
    void shouldBuildCustomNamespacedCacheKey() {
        CacheKey key = CacheKey.withNamespace("custom", "cache", "lock", "order", 1);

        assertEquals("custom:cache:lock:order:1", key.value());
    }
}
