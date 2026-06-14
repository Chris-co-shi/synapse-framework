package com.indigo.synapse.mq.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageSpecificExceptionTest {

    @Test
    void shouldCreateNonRetryableValidationException() {
        MessageValidationException exception = new MessageValidationException(
                MessageErrorCode.MESSAGE_HEADER_INVALID,
                "header invalid"
        );

        assertSame(MessageErrorCode.MESSAGE_HEADER_INVALID, exception.errorCode());
        assertFalse(exception.retryable());
    }

    @Test
    void shouldCreateNonRetryableSerializationException() {
        MessageSerializationException exception = new MessageSerializationException(
                MessageErrorCode.MESSAGE_DESERIALIZATION_FAILED,
                "payload invalid"
        );

        assertSame(MessageErrorCode.MESSAGE_DESERIALIZATION_FAILED, exception.errorCode());
        assertFalse(exception.retryable());
    }

    @Test
    void shouldCreateRetryablePublishException() {
        IllegalStateException cause = new IllegalStateException("broker unavailable");
        MessagePublishException exception = new MessagePublishException("publish failed", cause);

        assertSame(MessageErrorCode.MESSAGE_PUBLISH_FAILED, exception.errorCode());
        assertTrue(exception.retryable());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldAllowConsumeRetryableDecision() {
        assertTrue(new MessageConsumeException("consume failed", true).retryable());
        assertFalse(new MessageConsumeException("consume failed", false).retryable());
    }

    @Test
    void shouldCreateNonRetryableRoutingException() {
        MessageRoutingException exception = new MessageRoutingException("route failed");

        assertSame(MessageErrorCode.MESSAGE_ROUTING_FAILED, exception.errorCode());
        assertFalse(exception.retryable());
    }

    @Test
    void shouldCreateNonRetryableContextPropagationException() {
        MessageContextPropagationException exception = new MessageContextPropagationException("context invalid");

        assertSame(MessageErrorCode.MESSAGE_CONTEXT_PROPAGATION_FAILED, exception.errorCode());
        assertFalse(exception.retryable());
    }

    @Test
    void shouldAllowIdempotencyRetryableDecision() {
        assertTrue(new MessageIdempotencyException("idempotency backend unavailable", true).retryable());
        assertFalse(new MessageIdempotencyException("idempotent key invalid", false).retryable());
    }
}
