package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.messaging.context.MessageContextHeaders;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageConsumeResult;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageConsumeTemplateTest {

    @AfterEach
    void tearDown() {
        OperationContextHolder.clear();
    }

    @Test
    void shouldCallHandlerWithRestoredContext() {
        MessageConsumeTemplate template = new MessageConsumeTemplate(
                new OperationContextMessagePropagator(),
                new DefaultMessageExceptionClassifier()
        );

        MessageConsumeResult result = template.consume(envelope(Map.of(
                MessageContextHeaders.ACTOR_TYPE, "USER",
                MessageContextHeaders.ACTOR_ID, "actor-1",
                MessageContextHeaders.TRACE_ID, "trace-1"
        )), message -> {
            assertEquals("actor-1", OperationContextHolder.current().orElseThrow().actor().id());
            assertEquals("trace-1", OperationContextHolder.current().orElseThrow().traceId());
            return MessageConsumeResult.success();
        });

        assertTrue(result.isSuccess());
        assertTrue(OperationContextHolder.current().isEmpty());
    }

    @Test
    void shouldDiscardWhenHandlerReturnsNull() {
        MessageConsumeTemplate template = new MessageConsumeTemplate(
                new OperationContextMessagePropagator(),
                new DefaultMessageExceptionClassifier()
        );

        MessageConsumeResult result = template.consume(envelope(Map.of()), message -> null);

        assertTrue(result.isDiscardable());
        assertEquals("message handler returned null", result.reason());
    }

    @Test
    void shouldClassifyHandlerExceptionAndSkipTargetSuccess() {
        AtomicBoolean executed = new AtomicBoolean(false);
        MessageConsumeTemplate template = new MessageConsumeTemplate(
                new OperationContextMessagePropagator(),
                throwable -> {
                    executed.set(true);
                    return MessageConsumeResult.retry("classified");
                }
        );

        MessageConsumeResult result = template.consume(envelope(Map.of()), message -> {
            throw new IllegalStateException("failed");
        });

        assertTrue(executed.get());
        assertTrue(result.isRetryable());
        assertEquals("classified", result.reason());
    }

    @Test
    void shouldValidateArguments() {
        MessageConsumeTemplate template = new MessageConsumeTemplate(
                new OperationContextMessagePropagator(),
                new DefaultMessageExceptionClassifier()
        );

        assertThrows(IllegalArgumentException.class, () -> new MessageConsumeTemplate(null, new DefaultMessageExceptionClassifier()));
        assertThrows(IllegalArgumentException.class, () -> new MessageConsumeTemplate(new OperationContextMessagePropagator(), null));
        assertThrows(IllegalArgumentException.class, () -> template.consume(null, message -> MessageConsumeResult.success()));
        assertThrows(IllegalArgumentException.class, () -> template.consume(envelope(Map.of()), null));
    }

    @Test
    void shouldClassifyInvalidPayloadAsDiscard() {
        MessageConsumeTemplate template = new MessageConsumeTemplate(
                new OperationContextMessagePropagator(),
                new DefaultMessageExceptionClassifier()
        );

        MessageConsumeResult result = template.consume(envelope(Map.of()), message -> {
            throw new IllegalArgumentException("invalid payload");
        });

        assertFalse(result.isRetryable());
        assertTrue(result.isDiscardable());
    }

    private static MessageEnvelope envelope(Map<String, String> headers) {
        return new MessageEnvelope(
                "message-1",
                "sample.created",
                "sample-topic",
                "sample-tag",
                "sample-key",
                "sample-idempotent",
                "sample-service",
                "application/json",
                "v1",
                headers,
                "{}",
                null,
                null,
                Instant.parse("2026-06-14T00:00:00Z"),
                Instant.parse("2026-06-14T00:00:01Z")
        );
    }
}
