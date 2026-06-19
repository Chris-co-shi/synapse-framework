package com.indigo.synapse.messaging.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagePublishResultTest {

    @Test
    void shouldCreateSuccessResult() {
        MessagePublishResult result = MessagePublishResult.success("message-1", "broker-1");

        assertEquals(MessagePublishResult.Status.SUCCESS, result.status());
        assertEquals("message-1", result.messageId());
        assertEquals("broker-1", result.brokerMessageId());
        assertTrue(result.isSuccess());
    }

    @Test
    void shouldCreateFailureResult() {
        MessagePublishResult result = MessagePublishResult.failure("message-1", "publish failed");

        assertEquals(MessagePublishResult.Status.FAILED, result.status());
        assertEquals("publish failed", result.reason());
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNormalizeReasonAndValidateStatus() {
        MessagePublishResult result = new MessagePublishResult(MessagePublishResult.Status.FAILED, "message-1", null, null);

        assertEquals("", result.reason());
        assertThrows(IllegalArgumentException.class, () -> new MessagePublishResult(null, "message-1", null, null));
    }
}
