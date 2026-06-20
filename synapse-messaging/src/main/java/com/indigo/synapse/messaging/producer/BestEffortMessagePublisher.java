package com.indigo.synapse.messaging.producer;

import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import com.indigo.synapse.messaging.transport.MessageTransport;
import com.indigo.synapse.messaging.transport.MessageTransportResult;

import java.util.Objects;

/**
 * 直接委托 Transport 的尽力发布器。
 *
 * <p>该发布器不持久化消息，也不承诺 Broker 故障后的恢复；需要可靠投递时应使用
 * {@link ReliableMessagePublisher}。</p>
 */
public final class BestEffortMessagePublisher implements MessagePublisher {
    private final MessageTransport transport;
    private final OperationContextMessagePropagator propagator;

    public BestEffortMessagePublisher(MessageTransport transport, OperationContextMessagePropagator propagator) {
        this.transport = Objects.requireNonNull(transport, "transport must not be null");
        this.propagator = Objects.requireNonNull(propagator, "propagator must not be null");
    }

    @Override
    public MessagePublishResult publish(MessageEnvelope envelope) {
        MessageEnvelope propagated = propagator.withCurrentContext(envelope);
        MessageTransportResult result = transport.send(propagated);
        return result.accepted()
                ? MessagePublishResult.sent(propagated.metadata().messageId(), result.transportMessageId())
                : MessagePublishResult.failed(propagated.metadata().messageId(), result.reason());
    }
}
