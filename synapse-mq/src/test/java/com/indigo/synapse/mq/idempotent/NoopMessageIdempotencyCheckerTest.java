package com.indigo.synapse.mq.idempotent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class NoopMessageIdempotencyCheckerTest {

    @Test
    void shouldNeverMarkMessageAsProcessed() {
        NoopMessageIdempotencyChecker checker = new NoopMessageIdempotencyChecker();

        assertFalse(checker.isProcessed("sample-idempotent"));
        checker.markProcessed("sample-idempotent");
        assertFalse(checker.isProcessed("sample-idempotent"));
    }
}
