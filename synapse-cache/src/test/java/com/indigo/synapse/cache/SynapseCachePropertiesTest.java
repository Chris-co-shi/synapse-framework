package com.indigo.synapse.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SynapseCachePropertiesTest {

    @Test
    void shouldExposeDefaultValues() {
        SynapseCacheProperties properties = new SynapseCacheProperties();

        assertEquals(true, properties.getL1().isEnabled());
        assertEquals(Duration.ofMinutes(5), properties.getL1().getExpireAfterWrite());
        assertEquals(1_000L, properties.getL1().getMaximumSize());
        assertEquals(Duration.ofMinutes(30), properties.getL2().getTtl());
    }

    @Test
    void shouldConvertToCacheSpec() {
        SynapseCacheProperties properties = new SynapseCacheProperties();
        properties.getL1().setExpireAfterWrite(Duration.ofMinutes(2));
        properties.getL1().setMaximumSize(128);
        properties.getL2().setTtl(Duration.ofMinutes(15));

        CacheSpec cacheSpec = properties.toCacheSpec();

        assertEquals(Duration.ofMinutes(2), cacheSpec.l1Ttl());
        assertEquals(128L, cacheSpec.l1MaximumSize());
        assertEquals(Duration.ofMinutes(15), cacheSpec.l2Ttl());
    }
}
