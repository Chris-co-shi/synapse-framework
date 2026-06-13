package com.indigo.synapse.message.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExponentialBackoffRetryPolicyTest {

    @Test
    void shouldCalculateNextRetryTime() {
        ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(5, Duration.ofSeconds(10), 2.0d, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-06-13T00:00:00Z");
        ReliableMessage message = message(1, now);

        RetryDecision decision = policy.decide(message, new IllegalStateException("failed"), now);

        assertTrue(decision.retryable());
        assertEquals(now.plusSeconds(20), decision.nextRetryAt());
    }

    @Test
    void shouldExhaustWhenNextAttemptReachesMaxAttempts() {
        ExponentialBackoffRetryPolicy policy = new ExponentialBackoffRetryPolicy(3, Duration.ofSeconds(10), 2.0d, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-06-13T00:00:00Z");

        RetryDecision decision = policy.decide(message(2, now), new IllegalStateException("failed"), now);

        assertFalse(decision.retryable());
        assertTrue(decision.exhausted());
    }

    @Test
    void shouldValidatePolicyInput() {
        assertThrows(IllegalArgumentException.class, () -> new ExponentialBackoffRetryPolicy(0, Duration.ofSeconds(1), 2.0d, Duration.ofSeconds(10)));
        assertThrows(IllegalArgumentException.class, () -> new ExponentialBackoffRetryPolicy(1, Duration.ZERO, 2.0d, Duration.ofSeconds(10)));
        assertThrows(IllegalArgumentException.class, () -> new ExponentialBackoffRetryPolicy(1, Duration.ofSeconds(1), 0.5d, Duration.ofSeconds(10)));
    }

    private static ReliableMessage message(int attempt, Instant now) {
        MessageEnvelope envelope = MessageEnvelope.create("topic", null, null, Map.of(), "payload", null, null);
        return new ReliableMessage(envelope.messageId(), envelope, ReliableMessageStatus.RETRY, attempt, now, null, null, null, null, now, now, 0);
    }
}
