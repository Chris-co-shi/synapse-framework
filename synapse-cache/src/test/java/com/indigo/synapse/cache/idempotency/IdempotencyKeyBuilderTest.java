package com.indigo.synapse.cache.idempotency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdempotencyKeyBuilderTest {

    @Test
    void shouldBuildStableHashedKey() {
        String key = IdempotencyKeyBuilder.build("operation:create", "key-1");

        assertTrue(key.startsWith("synapse:idempotency:"));
        assertEquals(key, IdempotencyKeyBuilder.build(" operation:create ", " key-1 "));
    }

    @Test
    void shouldRejectBlankParts() {
        assertThrows(IllegalArgumentException.class, () -> IdempotencyKeyBuilder.build("", "key"));
        assertThrows(IllegalArgumentException.class, () -> IdempotencyKeyBuilder.build("scope", " "));
    }
}
