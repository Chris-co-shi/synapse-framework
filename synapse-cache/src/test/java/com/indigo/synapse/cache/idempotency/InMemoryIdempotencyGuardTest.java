package com.indigo.synapse.cache.idempotency;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryIdempotencyGuardTest {

    @Test
    void shouldRejectDuplicateWithinTtl() {
        InMemoryIdempotencyGuard guard = new InMemoryIdempotencyGuard();

        assertTrue(guard.tryAcquire("operation:create", "key-1", Duration.ofMinutes(5)));
        assertFalse(guard.tryAcquire("operation:create", "key-1", Duration.ofMinutes(5)));
        assertTrue(guard.tryAcquire("operation:update", "key-1", Duration.ofMinutes(5)));
    }

    @Test
    void shouldAllowAcquireAfterExpired() {
        MutableClock clock = new MutableClock(Instant.parse("2026-05-21T00:00:00Z"));
        InMemoryIdempotencyGuard guard = new InMemoryIdempotencyGuard(clock);

        assertTrue(guard.tryAcquire("operation:create", "key-1", Duration.ofSeconds(1)));
        clock.advance(Duration.ofSeconds(2));

        assertTrue(guard.tryAcquire("operation:create", "key-1", Duration.ofSeconds(1)));
    }

    @Test
    void shouldRejectInvalidTtl() {
        InMemoryIdempotencyGuard guard = new InMemoryIdempotencyGuard();

        assertThrows(IllegalArgumentException.class, () -> guard.tryAcquire("scope", "key", Duration.ZERO));
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        private void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
