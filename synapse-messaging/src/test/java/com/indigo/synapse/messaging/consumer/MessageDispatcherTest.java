package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.messaging.MessageFixtures;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageHandleResult;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDispatcherTest {
    @Test
    void shouldDispatchAndMarkEventAsProcessed() {
        AtomicBoolean processed = new AtomicBoolean();
        MessageIdempotencyStore store = new MessageIdempotencyStore() {
            public boolean isProcessed(String key) { return processed.get(); }
            public void markProcessed(String key) { processed.set(true); }
        };
        MessageHandler handler = new MessageHandler() {
            public String messageType() { return "order.created"; }
            public MessageHandleResult handle(com.indigo.synapse.messaging.core.MessageEnvelope envelope) {
                return MessageHandleResult.success();
            }
        };
        MessageDispatcher dispatcher = new MessageDispatcher(new MessageHandlerRegistry(List.of(handler)),
                new OperationContextMessagePropagator(), store, null, null);

        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 1).status())
                .isEqualTo(MessageHandleResult.Status.SUCCESS);
        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 2).status())
                .isEqualTo(MessageHandleResult.Status.DUPLICATE);
    }

    @Test
    void shouldApplyRetryPolicyAndRecordTerminalFailure() {
        AtomicBoolean recorded = new AtomicBoolean();
        MessageHandler handler = new MessageHandler() {
            public String messageType() { return "order.created"; }
            public MessageHandleResult handle(com.indigo.synapse.messaging.core.MessageEnvelope envelope) {
                throw new IllegalStateException("broken");
            }
        };
        MessageDispatcher dispatcher = new MessageDispatcher(new MessageHandlerRegistry(List.of(handler)),
                new OperationContextMessagePropagator(), null, failure -> recorded.set(true),
                (envelope, failure, attempt) -> attempt < 2);

        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 1).status())
                .isEqualTo(MessageHandleResult.Status.RETRY);
        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 2).status())
                .isEqualTo(MessageHandleResult.Status.DISCARD);
        assertThat(recorded).isTrue();
    }
}
