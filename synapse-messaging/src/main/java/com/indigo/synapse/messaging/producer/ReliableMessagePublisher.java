package com.indigo.synapse.messaging.producer;

import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import com.indigo.synapse.messaging.reliability.OutboxStore;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * 在当前本地事务中登记 Outbox 的可靠发布器。
 *
 * <p>该入口不直连 Broker。后台投递者可重复发送，因此语义为 At-least-once；消费方必须按
 * {@code eventId}（优先）或 {@code messageId} 幂等。</p>
 */
public final class ReliableMessagePublisher implements MessagePublisher {
    private final OutboxStore outboxStore;
    private final OperationContextMessagePropagator propagator;

    public ReliableMessagePublisher(OutboxStore outboxStore, OperationContextMessagePropagator propagator) {
        this.outboxStore = Objects.requireNonNull(outboxStore, "outboxStore must not be null");
        this.propagator = Objects.requireNonNull(propagator, "propagator must not be null");
    }

    @Override
    public MessagePublishResult publish(MessageEnvelope envelope) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Reliable message publishing requires an active local transaction");
        }
        MessageEnvelope propagated = propagator.withCurrentContext(envelope);
        outboxStore.append(propagated);
        return MessagePublishResult.stored(propagated.metadata().messageId());
    }
}
