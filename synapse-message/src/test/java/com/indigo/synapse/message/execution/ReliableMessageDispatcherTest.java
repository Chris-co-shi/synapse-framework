package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.MessagePublishResult;
import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.core.ReliableMessageStatus;
import com.indigo.synapse.message.core.RetryDecision;
import com.indigo.synapse.message.core.RetryPolicy;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.MessageTransport;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReliableMessageDispatcherTest {

    private static final Instant NOW = Instant.parse("2026-06-13T00:00:00Z");

    @Test
    void shouldMarkSentWhenTransportSucceeded() {
        RecordingRepository repository = new RecordingRepository(List.of(message(0)));
        ReliableMessageDispatcher dispatcher = dispatcher(repository, envelope -> new MessagePublishResult(envelope.topic(), envelope.messageId(), 1), (message, failure, now) -> RetryDecision.notRetryable());

        int dispatched = dispatcher.dispatchDue("worker-1", 10, Duration.ofSeconds(30));

        assertEquals(1, dispatched);
        assertEquals(List.of("sent:" + repository.messages.get(0).messageId()), repository.events);
    }

    @Test
    void shouldRetryWhenPolicyReturnsRetryableDecision() {
        RecordingRepository repository = new RecordingRepository(List.of(message(0)));
        ReliableMessageDispatcher dispatcher = dispatcher(repository, envelope -> {
            throw new IllegalStateException("temporary");
        }, (message, failure, now) -> RetryDecision.retryAt(now.plusSeconds(10)));

        dispatcher.dispatchDue("worker-1", 10, Duration.ofSeconds(30));

        assertEquals(List.of("retry:" + repository.messages.get(0).messageId() + ":1"), repository.events);
    }

    @Test
    void shouldMoveToDeadLetterWhenRetryExhausted() {
        ReliableMessage message = message(4);
        RecordingRepository repository = new RecordingRepository(List.of(message));
        RecordingDeadLetterRepository deadLetters = new RecordingDeadLetterRepository();
        ReliableMessageDispatcher dispatcher = new ReliableMessageDispatcher(
                repository,
                deadLetters,
                envelope -> {
                    throw new IllegalStateException("exhausted");
                },
                (reliableMessage, failure, now) -> RetryDecision.exhaustedDecision(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        dispatcher.dispatchDue("worker-1", 10, Duration.ofSeconds(30));

        assertEquals(List.of("dlq:" + message.messageId() + ":5"), repository.events);
        assertEquals(List.of(message.messageId()), deadLetters.messageIds);
    }

    private static ReliableMessageDispatcher dispatcher(RecordingRepository repository, MessageTransport transport, RetryPolicy retryPolicy) {
        return new ReliableMessageDispatcher(
                repository,
                new RecordingDeadLetterRepository(),
                transport,
                retryPolicy,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static ReliableMessage message(int attempt) {
        MessageEnvelope envelope = MessageEnvelope.create("topic", null, null, Map.of(), "payload", null, null);
        return new ReliableMessage(envelope.messageId(), envelope, ReliableMessageStatus.PENDING, attempt, NOW, null, null, null, null, NOW, NOW, 0);
    }

    private static final class RecordingRepository implements ReliableMessageRepository {

        private final List<ReliableMessage> messages;
        private final List<String> events = new ArrayList<>();

        private RecordingRepository(List<ReliableMessage> messages) {
            this.messages = messages;
        }

        @Override
        public void save(ReliableMessage message) {
        }

        @Override
        public List<ReliableMessage> claimDueMessages(String workerId, Instant now, Instant lockedUntil, int batchSize) {
            return messages;
        }

        @Override
        public void markSent(String messageId, Instant now) {
            events.add("sent:" + messageId);
        }

        @Override
        public void markRetry(String messageId, int attempt, Instant nextRetryAt, String lastError, Instant now) {
            events.add("retry:" + messageId + ":" + attempt);
        }

        @Override
        public void markDeadLetter(String messageId, int attempt, String lastError, Instant now) {
            events.add("dlq:" + messageId + ":" + attempt);
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

    private static final class RecordingDeadLetterRepository implements DeadLetterRepository {

        private final List<String> messageIds = new ArrayList<>();

        @Override
        public void save(ReliableMessage message, String reason) {
            messageIds.add(message.messageId());
        }
    }
}
