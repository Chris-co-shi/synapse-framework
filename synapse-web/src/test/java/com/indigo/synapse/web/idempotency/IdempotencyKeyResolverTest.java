package com.indigo.synapse.web.idempotency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class IdempotencyKeyResolverTest {

    @Test
    void shouldResolveSafeIdempotencyKey() {
        assertEquals("operation:create-1", IdempotencyKeyResolver.resolve(" operation:create-1 ").orElseThrow());
    }

    @Test
    void shouldRejectMissingOrUnsafeKey() {
        assertTrue(IdempotencyKeyResolver.resolve(null).isEmpty());
        assertTrue(IdempotencyKeyResolver.resolve(" ").isEmpty());
        assertTrue(IdempotencyKeyResolver.resolve("key\r\nbad").isEmpty());
        assertTrue(IdempotencyKeyResolver.resolve("key/bad").isEmpty());
        assertTrue(IdempotencyKeyResolver.resolve("x".repeat(129)).isEmpty());
    }

    @Test
    void shouldValidateKeyCharacters() {
        assertTrue(IdempotencyKeyResolver.isValid("abc-ABC_123.456:789"));
        assertFalse(IdempotencyKeyResolver.isValid("abc/123"));
    }
}
