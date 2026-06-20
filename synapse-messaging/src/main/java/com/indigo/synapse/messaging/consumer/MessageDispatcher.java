package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessageHandleResult;
import com.indigo.synapse.messaging.reliability.MessageFailure;
import com.indigo.synapse.messaging.reliability.MessageFailureStore;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import com.indigo.synapse.messaging.reliability.MessageRetryPolicy;

import java.time.Instant;
import java.util.Objects;

/**
 * 消息消费分发器，统一执行上下文恢复、Handler 路由、幂等检查和失败决策。
 *
 * <p>Broker 可能重复投递。只有 Handler 成功后才标记幂等键，键优先使用 eventId，缺失时使用
 * messageId。并发去重的原子性由应用提供的 {@link MessageIdempotencyStore} 保证。</p>
 */
public final class MessageDispatcher {
    private final MessageHandlerRegistry registry;
    private final OperationContextMessagePropagator propagator;
    private final MessageIdempotencyStore idempotencyStore;
    private final MessageFailureStore failureStore;
    private final MessageRetryPolicy retryPolicy;

    public MessageDispatcher(MessageHandlerRegistry registry, OperationContextMessagePropagator propagator,
                             MessageIdempotencyStore idempotencyStore, MessageFailureStore failureStore,
                             MessageRetryPolicy retryPolicy) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.propagator = Objects.requireNonNull(propagator, "propagator must not be null");
        this.idempotencyStore = idempotencyStore;
        this.failureStore = failureStore;
        this.retryPolicy = retryPolicy;
    }

    public MessageHandleResult dispatch(MessageEnvelope envelope, int attempt) {
        Objects.requireNonNull(envelope, "envelope must not be null");
        if (attempt < 1) throw new IllegalArgumentException("attempt must be positive");
        String key = envelope.idempotencyKey();
        if (idempotencyStore != null && idempotencyStore.isProcessed(key)) return MessageHandleResult.duplicate();
        MessageHandler handler = registry.find(envelope.metadata().messageType()).orElse(null);
        if (handler == null) return MessageHandleResult.discard("No MessageHandler for " + envelope.metadata().messageType());
        try (OperationContextScope ignored = propagator.restore(envelope)) {
            MessageHandleResult result = handler.handle(envelope);
            if (result == null) return MessageHandleResult.discard("MessageHandler returned null");
            if (result.status() == MessageHandleResult.Status.SUCCESS && idempotencyStore != null) {
                idempotencyStore.markProcessed(key);
            }
            return result;
        } catch (RuntimeException failure) {
            boolean retry = retryPolicy == null || retryPolicy.shouldRetry(envelope, failure, attempt);
            if (!retry && failureStore != null) {
                failureStore.record(new MessageFailure(envelope.metadata().messageId(), envelope.metadata().eventId(),
                        envelope.metadata().messageType(), attempt, failure.getClass().getName(),
                        failure.getMessage(), Instant.now()));
            }
            return retry ? MessageHandleResult.retry(summary(failure)) : MessageHandleResult.discard(summary(failure));
        }
    }

    private static String summary(Throwable failure) {
        return failure.getClass().getSimpleName() + (failure.getMessage() == null ? "" : ": " + failure.getMessage());
    }
}
