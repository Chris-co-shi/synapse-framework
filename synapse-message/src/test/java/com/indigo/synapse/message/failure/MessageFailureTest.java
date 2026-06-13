package com.indigo.synapse.message.failure;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.exception.MessageConsumeException;
import com.indigo.synapse.message.exception.MessageException;
import com.indigo.synapse.message.exception.MessagePublishException;
import com.indigo.synapse.message.exception.MessageSerializationException;
import com.indigo.synapse.message.port.CompensationPort;
import com.indigo.synapse.message.port.DeadLetterPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageFailureTest {

    @Test
    void shouldRepresentFailureContextAndStrategy() {
        MessageFailure failure = failure();
        MessageFailureContext context = new MessageFailureContext(failure, 2, Map.of("handler", "test"));
        MessageFailureHandler handler = ignored -> MessageFailureStrategy.DEAD_LETTER;

        assertEquals(failure, context.failure());
        assertEquals(2, context.attempt());
        assertEquals("test", context.attributes().get("handler"));
        assertEquals(MessageFailureStrategy.DEAD_LETTER, handler.handle(context));
        assertThrows(UnsupportedOperationException.class, () -> context.attributes().put("x", "y"));
    }

    @Test
    void shouldExposeReporterDeadLetterAndCompensationPorts() {
        AtomicReference<MessageFailure> reported = new AtomicReference<>();
        AtomicReference<MessageFailure> deadLetter = new AtomicReference<>();
        AtomicReference<MessageFailure> compensated = new AtomicReference<>();
        MessageFailure failure = failure();

        MessageErrorReporter reporter = reported::set;
        DeadLetterPort deadLetterPort = deadLetter::set;
        CompensationPort compensationPort = compensated::set;

        reporter.report(failure);
        deadLetterPort.sendToDeadLetter(failure);
        compensationPort.compensate(failure);

        assertEquals(failure, reported.get());
        assertEquals(failure, deadLetter.get());
        assertEquals(failure, compensated.get());
    }

    @Test
    void shouldValidateFailureModels() {
        MessageFailure failure = failure();

        assertThrows(IllegalArgumentException.class, () -> new MessageFailure(null, "consume", new RuntimeException(), Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new MessageFailure(message(), "", new RuntimeException(), Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new MessageFailure(message(), "consume", null, Instant.now()));
        assertThrows(IllegalArgumentException.class, () -> new MessageFailure(message(), "consume", new RuntimeException(), null));
        assertThrows(IllegalArgumentException.class, () -> new MessageFailureContext(null, 0, Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new MessageFailureContext(failure, -1, Map.of()));
    }

    @Test
    void shouldExposeMessageExceptions() {
        RuntimeException cause = new RuntimeException("root");

        assertInstanceOf(MessageException.class, new MessagePublishException("publish failed", cause));
        assertInstanceOf(MessageException.class, new MessageConsumeException("consume failed", cause));
        assertInstanceOf(MessageException.class, new MessageSerializationException("serialize failed", cause));
    }

    private static MessageFailure failure() {
        return new MessageFailure(message(), "consume", new IllegalStateException("failed"), Instant.parse("2026-05-20T10:00:00Z"));
    }

    private static MessageEnvelope message() {
        return new MessageEnvelope(
                "message-1",
                "topic-1",
                "tag",
                "key",
                Map.of(),
                "payload",
                "trace-1",
                "tenant-a",
                Instant.parse("2026-05-20T09:59:59Z")
        );
    }
}
