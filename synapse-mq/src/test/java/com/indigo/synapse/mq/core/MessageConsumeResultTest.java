package com.indigo.synapse.mq.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageConsumeResultTest {

    @Test
    void shouldCreateStatusResults() {
        MessageConsumeResult success = MessageConsumeResult.success();
        MessageConsumeResult retry = MessageConsumeResult.retry("retry later");
        MessageConsumeResult discard = MessageConsumeResult.discard("discard message");

        assertEquals(MessageConsumeResult.Status.SUCCESS, success.status());
        assertTrue(success.isSuccess());
        assertFalse(success.isRetryable());
        assertFalse(success.isDiscardable());

        assertEquals(MessageConsumeResult.Status.RETRY, retry.status());
        assertTrue(retry.isRetryable());

        assertEquals(MessageConsumeResult.Status.DISCARD, discard.status());
        assertTrue(discard.isDiscardable());
    }

    @Test
    void shouldNormalizeReasonAndValidateStatus() {
        MessageConsumeResult result = MessageConsumeResult.retry(null);

        assertEquals("", result.reason());
        assertThrows(NullPointerException.class, () -> new MessageConsumeResult(null, null));
    }
}
