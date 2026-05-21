package com.indigo.synapse.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheSpecTest {

    @Test
    void shouldProvideConservativeDefaults() {
        CacheSpec defaults = CacheSpec.defaults();

        assertEquals(Duration.ofMinutes(5), defaults.l1Ttl());
        assertEquals(1_000L, defaults.l1MaximumSize());
        assertEquals(Duration.ofMinutes(30), defaults.l2Ttl());
    }

    @Test
    void shouldValidateInput() {
        assertThrows(IllegalArgumentException.class, () -> new CacheSpec(Duration.ZERO, 1, Duration.ofMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> new CacheSpec(Duration.ofMinutes(1), 0, Duration.ofMinutes(1)));
        assertThrows(IllegalArgumentException.class, () -> new CacheSpec(Duration.ofMinutes(1), 1, Duration.ZERO));
    }
}
