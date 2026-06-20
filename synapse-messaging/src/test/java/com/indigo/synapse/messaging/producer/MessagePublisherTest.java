package com.indigo.synapse.messaging.producer;

import com.indigo.synapse.messaging.MessageFixtures;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import com.indigo.synapse.messaging.transport.MessageTransportResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagePublisherTest {
    @AfterEach
    void clearTransactionState() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void bestEffortShouldDelegateToTransport() {
        AtomicReference<MessageEnvelope> sent = new AtomicReference<>();
        MessageEnvelope envelope = MessageFixtures.envelope();
        BestEffortMessagePublisher publisher = new BestEffortMessagePublisher(message -> {
            sent.set(message);
            return MessageTransportResult.accepted("transport-1");
        }, new OperationContextMessagePropagator());

        MessagePublishResult result = publisher.publish(envelope);

        assertThat(result.status()).isEqualTo(MessagePublishResult.Status.SENT);
        assertThat(sent).hasValue(envelope);
    }

    @Test
    void reliableShouldRejectPublishingWithoutLocalTransaction() {
        ReliableMessagePublisher publisher = new ReliableMessagePublisher(envelope -> { },
                new OperationContextMessagePropagator());
        assertThatThrownBy(() -> publisher.publish(MessageFixtures.envelope()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("active local transaction");
    }

    @Test
    void reliableShouldOnlyAppendOutboxWithinTransaction() {
        AtomicReference<MessageEnvelope> stored = new AtomicReference<>();
        MessageEnvelope envelope = MessageFixtures.envelope();
        ReliableMessagePublisher publisher = new ReliableMessagePublisher(stored::set,
                new OperationContextMessagePropagator());
        TransactionSynchronizationManager.setActualTransactionActive(true);

        MessagePublishResult result = publisher.publish(envelope);

        assertThat(result.status()).isEqualTo(MessagePublishResult.Status.STORED);
        assertThat(stored).hasValue(envelope);
    }
}
