package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.core.RetryDecision;
import com.indigo.synapse.message.core.RetryPolicy;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.MessageTransport;
import com.indigo.synapse.message.port.ReliableMessageRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 可靠消息派发器。
 *
 * <p>派发器负责 claim due 消息、调用 MQ 无关 transport，并根据结果更新状态。
 * 多集群并发控制由仓储实现的租约和版本条件保障。</p>
 */
public final class ReliableMessageDispatcher {

    private final ReliableMessageRepository repository;
    private final DeadLetterRepository deadLetterRepository;
    private final MessageTransport messageTransport;
    private final RetryPolicy retryPolicy;
    private final Clock clock;

    public ReliableMessageDispatcher(
            ReliableMessageRepository repository,
            DeadLetterRepository deadLetterRepository,
            MessageTransport messageTransport,
            RetryPolicy retryPolicy,
            Clock clock
    ) {
        if (repository == null || deadLetterRepository == null || messageTransport == null || retryPolicy == null) {
            throw new IllegalArgumentException("dispatcher dependencies must not be null");
        }
        this.repository = repository;
        this.deadLetterRepository = deadLetterRepository;
        this.messageTransport = messageTransport;
        this.retryPolicy = retryPolicy;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public int dispatchDue(String workerId, int batchSize, Duration lockTtl) {
        validate(workerId, batchSize, lockTtl);
        Instant now = clock.instant();
        List<ReliableMessage> messages = repository.claimDueMessages(workerId, now, now.plus(lockTtl), batchSize);
        for (ReliableMessage message : messages) {
            dispatchOne(message, now);
        }
        return messages.size();
    }

    private void dispatchOne(ReliableMessage message, Instant now) {
        try {
            messageTransport.send(message.envelope());
            repository.markSent(message.messageId(), now);
        } catch (RuntimeException failure) {
            RetryDecision decision = retryPolicy.decide(message, failure, now);
            String errorMessage = failure.getMessage();
            if (decision.retryable()) {
                repository.markRetry(message.messageId(), message.attempt() + 1, decision.nextRetryAt(), errorMessage, now);
                return;
            }
            repository.markDeadLetter(message.messageId(), message.attempt() + 1, errorMessage, now);
            deadLetterRepository.save(message, errorMessage);
        }
    }

    private static void validate(String workerId, int batchSize, Duration lockTtl) {
        if (workerId == null || workerId.isBlank()) {
            throw new IllegalArgumentException("workerId must not be blank");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive");
        }
        if (lockTtl == null || lockTtl.isZero() || lockTtl.isNegative()) {
            throw new IllegalArgumentException("lockTtl must be positive");
        }
    }
}
