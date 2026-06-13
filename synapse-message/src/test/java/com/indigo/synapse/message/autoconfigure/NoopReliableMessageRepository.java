package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.port.ReliableMessageRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

final class NoopReliableMessageRepository implements ReliableMessageRepository {

    @Override
    public void save(ReliableMessage message) {
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
