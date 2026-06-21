package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.messaging.MessageFixtures;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageHandleResult;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyClaim;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyKey;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class MessageDispatcherTest {
    private static final Duration LEASE = Duration.ofSeconds(30);

    @Test
    void shouldCompleteClaimAndTreatNextDeliveryAsDuplicate() {
        TestStore store = new TestStore();
        AtomicInteger handled = new AtomicInteger();
        MessageHandler handler = handler("orders-created", envelope -> {
            handled.incrementAndGet();
            return MessageHandleResult.success();
        });
        MessageDispatcher dispatcher = dispatcher(handler, store, "order-service");

        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 1).status())
                .isEqualTo(MessageHandleResult.Status.SUCCESS);
        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 2).status())
                .isEqualTo(MessageHandleResult.Status.DUPLICATE);
        assertThat(handled).hasValue(1);
        assertThat(store.lastKey.consumerId()).isEqualTo("order-service");
        assertThat(store.lastKey.handlerId()).isEqualTo("orders-created");
        assertThat(store.lastKey.messageType()).isEqualTo("order.created");
        assertThat(store.lastKey.messageIdentity()).isEqualTo("event-1");
    }

    @Test
    void shouldRetryWhileAnotherDeliveryIsProcessing() {
        AtomicBoolean handled = new AtomicBoolean();
        MessageIdempotencyStore store = new MessageIdempotencyStore() {
            public MessageIdempotencyClaim claim(MessageIdempotencyKey key, Duration lease) {
                return new MessageIdempotencyClaim(MessageIdempotencyClaim.Status.PROCESSING, null);
            }
            public boolean complete(MessageIdempotencyKey key, String claimId) { return false; }
            public boolean release(MessageIdempotencyKey key, String claimId) { return false; }
        };
        MessageDispatcher dispatcher = dispatcher(handler("orders-created", envelope -> {
            handled.set(true);
            return MessageHandleResult.success();
        }), store, "order-service");

        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 1).status())
                .isEqualTo(MessageHandleResult.Status.RETRY);
        assertThat(handled).isFalse();
    }

    @Test
    void shouldReleaseFailedClaimAndAllowRetry() {
        TestStore store = new TestStore();
        AtomicInteger attempts = new AtomicInteger();
        MessageHandler handler = handler("orders-created", envelope -> {
            if (attempts.incrementAndGet() == 1) throw new IllegalStateException("broken");
            return MessageHandleResult.success();
        });
        MessageDispatcher dispatcher = new MessageDispatcher(new MessageHandlerRegistry(List.of(handler)),
                new OperationContextMessagePropagator(), store, null,
                (envelope, failure, attempt) -> true, "order-service", LEASE);

        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 1).status())
                .isEqualTo(MessageHandleResult.Status.RETRY);
        assertThat(dispatcher.dispatch(MessageFixtures.envelope(), 2).status())
                .isEqualTo(MessageHandleResult.Status.SUCCESS);
        assertThat(attempts).hasValue(2);
    }

    @Test
    void shouldScopeSameEventByConsumerAndHandler() {
        TestStore store = new TestStore();
        AtomicInteger handled = new AtomicInteger();
        MessageHandler first = handler("handler-a", envelope -> {
            handled.incrementAndGet();
            return MessageHandleResult.success();
        });
        MessageHandler second = handler("handler-b", envelope -> {
            handled.incrementAndGet();
            return MessageHandleResult.success();
        });

        assertThat(dispatcher(first, store, "consumer-a").dispatch(MessageFixtures.envelope(), 1).isSuccess()).isTrue();
        assertThat(dispatcher(first, store, "consumer-b").dispatch(MessageFixtures.envelope(), 1).isSuccess()).isTrue();
        assertThat(dispatcher(second, store, "consumer-a").dispatch(MessageFixtures.envelope(), 1).isSuccess()).isTrue();
        assertThat(handled).hasValue(3);
    }

    private static MessageDispatcher dispatcher(
            MessageHandler handler, MessageIdempotencyStore store, String consumerId) {
        return new MessageDispatcher(new MessageHandlerRegistry(List.of(handler)),
                new OperationContextMessagePropagator(), store, null, null, consumerId, LEASE);
    }

    private static MessageHandler handler(String handlerId, HandlerBody body) {
        return new MessageHandler() {
            public String messageType() { return "order.created"; }
            public String handlerId() { return handlerId; }
            public MessageHandleResult handle(com.indigo.synapse.messaging.core.MessageEnvelope envelope) {
                return body.handle(envelope);
            }
        };
    }

    @FunctionalInterface
    private interface HandlerBody {
        MessageHandleResult handle(com.indigo.synapse.messaging.core.MessageEnvelope envelope);
    }

    private static final class TestStore implements MessageIdempotencyStore {
        private final Map<MessageIdempotencyKey, Entry> entries = new ConcurrentHashMap<>();
        private volatile MessageIdempotencyKey lastKey;

        public MessageIdempotencyClaim claim(MessageIdempotencyKey key, Duration lease) {
            lastKey = key;
            Holder holder = new Holder();
            entries.compute(key, (ignored, current) -> {
                Instant now = Instant.now();
                if (current == null || current.state == State.RETRYABLE
                        || current.state == State.PROCESSING && !current.expiresAt.isAfter(now)) {
                    String claimId = UUID.randomUUID().toString();
                    holder.claim = new MessageIdempotencyClaim(MessageIdempotencyClaim.Status.ACQUIRED, claimId);
                    return new Entry(State.PROCESSING, claimId, now.plus(lease));
                }
                holder.claim = current.state == State.COMPLETED
                        ? new MessageIdempotencyClaim(MessageIdempotencyClaim.Status.COMPLETED, null)
                        : new MessageIdempotencyClaim(MessageIdempotencyClaim.Status.PROCESSING, null);
                return current;
            });
            return holder.claim;
        }

        public boolean complete(MessageIdempotencyKey key, String claimId) {
            AtomicBoolean completed = new AtomicBoolean();
            entries.computeIfPresent(key, (ignored, current) -> {
                if (current.state == State.PROCESSING && current.claimId.equals(claimId)) {
                    completed.set(true);
                    return new Entry(State.COMPLETED, null, Instant.MAX);
                }
                return current;
            });
            return completed.get();
        }

        public boolean release(MessageIdempotencyKey key, String claimId) {
            AtomicBoolean released = new AtomicBoolean();
            entries.computeIfPresent(key, (ignored, current) -> {
                if (current.state == State.PROCESSING && current.claimId.equals(claimId)) {
                    released.set(true);
                    return new Entry(State.RETRYABLE, null, Instant.MIN);
                }
                return current;
            });
            return released.get();
        }
    }

    private static final class Holder {
        private MessageIdempotencyClaim claim;
    }

    private record Entry(State state, String claimId, Instant expiresAt) { }

    private enum State { PROCESSING, COMPLETED, RETRYABLE }
}
