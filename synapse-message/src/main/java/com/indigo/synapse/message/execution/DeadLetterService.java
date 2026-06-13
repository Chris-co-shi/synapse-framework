package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.port.ReliableMessageRepository;

import java.time.Clock;

/**
 * 死信操作服务。
 */
public final class DeadLetterService {

    private final ReliableMessageRepository repository;
    private final Clock clock;

    public DeadLetterService(ReliableMessageRepository repository, Clock clock) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public void requeue(String messageId) {
        validate(messageId);
        repository.requeue(messageId, clock.instant(), clock.instant());
    }

    public void ignore(String messageId) {
        validate(messageId);
        repository.markCancelled(messageId, clock.instant());
    }

    private static void validate(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId must not be blank");
        }
    }
}
