package com.indigo.synapse.message.execution;

import com.indigo.synapse.message.core.MessageEnvelope;
import com.indigo.synapse.message.core.ReliableMessage;
import com.indigo.synapse.message.port.ReliableMessageRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Outbox 写入入口。
 *
 * <p>消费方应在业务本地事务内调用该组件，使业务数据和 outbox 记录同库同事务提交。</p>
 */
public final class OutboxAppender {

    private final ReliableMessageRepository repository;
    private final Clock clock;

    public OutboxAppender(ReliableMessageRepository repository, Clock clock) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null");
        }
        this.repository = repository;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    public ReliableMessage append(MessageEnvelope envelope, String idempotencyKey) {
        Instant now = clock.instant();
        ReliableMessage message = ReliableMessage.pending(envelope, idempotencyKey, now);
        repository.save(message);
        return message;
    }
}
