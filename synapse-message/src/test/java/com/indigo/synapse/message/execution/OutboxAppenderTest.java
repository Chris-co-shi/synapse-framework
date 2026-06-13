package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboxAppenderTest {

    @Test
    void shouldAppendPendingMessage() {
        AtomicReference<ReliableMessage> saved = new AtomicReference<>();
        OutboxAppender appender = new OutboxAppender(new RecordingRepository(saved), Clock.fixed(Instant.parse("2026-06-13T00:00:00Z"), ZoneOffset.UTC));
        MessageEnvelope envelope = MessageEnvelope.create("topic", null, null, Map.of(), "payload", null, null);

        ReliableMessage message = appender.append(envelope, "idem-1");

        assertEquals(message, saved.get());
        assertEquals("idem-1", message.idempotencyKey());
    }

    @Test
    void shouldValidateDependencies() {
        assertThrows(IllegalArgumentException.class, () -> new OutboxAppender(null, Clock.systemUTC()));
    }

    private record RecordingRepository(AtomicReference<ReliableMessage> saved) implements ReliableMessageRepository {

        @Override
        public void save(ReliableMessage message) {
            saved.set(message);
        }

        @Override
        public List<ReliableMessage> claimDueMessages(String workerId, Instant now, Instant lockedUntil, int batchSize) {
            return List.of();
        }

        @Override
        public void markSent(String messageId, Instant now) {
        }

        @Override
        public void markRetry(String messageId, int attempt, Instant nextRetryAt, String lastError, Instant now) {
        }

        @Override
        public void markDeadLetter(String messageId, int attempt, String lastError, Instant now) {
        }

        @Override
        public void requeue(String messageId, Instant nextRetryAt, Instant now) {
        }

        @Override
        public void markCancelled(String messageId, Instant now) {
        }

        @Override
        public Optional<ReliableMessage> findByMessageId(String messageId) {
            return Optional.empty();
        }
    }
}
