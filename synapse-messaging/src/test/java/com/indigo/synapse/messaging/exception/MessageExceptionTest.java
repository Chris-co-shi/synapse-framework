package com.indigo.synapse.messaging.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageExceptionTest {

    @Test
    void shouldKeepErrorCodeMessageRetryableAndCause() {
        IllegalStateException cause = new IllegalStateException("origin");
        MessageException exception = new MessageException(
                MessageErrorCode.MESSAGE_CONSUME_FAILED,
                "consume failed",
                true,
                cause
        );

        assertSame(MessageErrorCode.MESSAGE_CONSUME_FAILED, exception.errorCode());
        assertEquals("consume failed", exception.getMessage());
        assertTrue(exception.retryable());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldDefaultToNonRetryable() {
        MessageException exception = new MessageException(MessageErrorCode.MESSAGE_INVALID);

        assertSame(MessageErrorCode.MESSAGE_INVALID, exception.errorCode());
        assertEquals("消息不合法", exception.getMessage());
        assertFalse(exception.retryable());
    }

    @Test
    void shouldRejectMissingErrorCode() {
        assertThrows(NullPointerException.class, () -> new MessageException(null));
        assertThrows(NullPointerException.class, () -> new MessageException(null, "message", false, null));
    }
}
