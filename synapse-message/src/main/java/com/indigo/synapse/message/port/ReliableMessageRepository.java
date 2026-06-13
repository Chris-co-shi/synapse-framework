package com.indigo.synapse.message.port;

import com.indigo.synapse.message.core.ReliableMessage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 可靠消息仓储端口。
 */
public interface ReliableMessageRepository {

    void save(ReliableMessage message);

    List<ReliableMessage> claimDueMessages(String workerId, Instant now, Instant lockedUntil, int batchSize);

    void markSent(String messageId, Instant now);

    void markRetry(String messageId, int attempt, Instant nextRetryAt, String lastError, Instant now);

    void markDeadLetter(String messageId, int attempt, String lastError, Instant now);

    void requeue(String messageId, Instant nextRetryAt, Instant now);

    void markCancelled(String messageId, Instant now);

    Optional<ReliableMessage> findByMessageId(String messageId);
}
