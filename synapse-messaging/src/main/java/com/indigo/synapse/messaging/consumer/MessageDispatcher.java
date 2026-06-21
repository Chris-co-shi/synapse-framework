package com.indigo.synapse.messaging.consumer;

import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessageHandleResult;
import com.indigo.synapse.messaging.reliability.MessageFailure;
import com.indigo.synapse.messaging.reliability.MessageFailureStore;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyClaim;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyKey;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import com.indigo.synapse.messaging.reliability.MessageRetryPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** 消息消费分发器，统一执行上下文恢复、Handler 路由、原子幂等和失败决策。 */
public final class MessageDispatcher {
    private final MessageHandlerRegistry registry;
    private final OperationContextMessagePropagator propagator;
    private final MessageIdempotencyStore idempotencyStore;
    private final MessageFailureStore failureStore;
    private final MessageRetryPolicy retryPolicy;
    private final String consumerId;
    private final Duration idempotencyLease;

    public MessageDispatcher(MessageHandlerRegistry registry, OperationContextMessagePropagator propagator,
                             MessageIdempotencyStore idempotencyStore, MessageFailureStore failureStore,
                             MessageRetryPolicy retryPolicy, String consumerId, Duration idempotencyLease) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.propagator = Objects.requireNonNull(propagator, "propagator must not be null");
        this.idempotencyStore = idempotencyStore;
        this.failureStore = failureStore;
        this.retryPolicy = retryPolicy;
        this.consumerId = idempotencyStore == null ? null : requireText(consumerId, "consumerId");
        this.idempotencyLease = idempotencyStore == null ? null : requirePositive(idempotencyLease);
    }

    public MessageHandleResult dispatch(MessageEnvelope envelope, int attempt) {
        Objects.requireNonNull(envelope, "envelope must not be null");
        if (attempt < 1) throw new IllegalArgumentException("attempt must be positive");
        MessageHandler handler = registry.find(envelope.metadata().messageType()).orElse(null);
        if (handler == null) return MessageHandleResult.discard("No MessageHandler for " + envelope.metadata().messageType());

        MessageIdempotencyKey key = null;
        String claimId = null;
        try {
            if (idempotencyStore != null) {
                key = new MessageIdempotencyKey(consumerId, handler.handlerId(),
                        envelope.metadata().messageType(), envelope.idempotencyKey());
                MessageIdempotencyClaim claim = Objects.requireNonNull(
                        idempotencyStore.claim(key, idempotencyLease), "idempotency claim must not be null");
                if (claim.status() == MessageIdempotencyClaim.Status.COMPLETED) {
                    return MessageHandleResult.duplicate();
                }
                if (claim.status() == MessageIdempotencyClaim.Status.PROCESSING) {
                    return MessageHandleResult.retry("Message is already being processed");
                }
                claimId = requireText(claim.claimId(), "claimId");
            }

            try (OperationContextScope ignored = propagator.restore(envelope)) {
                MessageHandleResult result = handler.handle(envelope);
                if (result == null) {
                    return releaseThen(key, claimId, MessageHandleResult.discard("MessageHandler returned null"));
                }
                if (result.status() == MessageHandleResult.Status.SUCCESS
                        || result.status() == MessageHandleResult.Status.DUPLICATE) {
                    if (idempotencyStore != null && !idempotencyStore.complete(key, claimId)) {
                        return MessageHandleResult.retry("Idempotency claim ownership was lost before completion");
                    }
                    return result;
                }
                return releaseThen(key, claimId, result);
            }
        } catch (RuntimeException failure) {
            releaseAfterFailure(key, claimId, failure);
            boolean retry = retryPolicy == null || retryPolicy.shouldRetry(envelope, failure, attempt);
            if (!retry && failureStore != null) {
                failureStore.record(new MessageFailure(envelope.metadata().messageId(), envelope.metadata().eventId(),
                        envelope.metadata().messageType(), attempt, failure.getClass().getName(),
                        failure.getMessage(), Instant.now()));
            }
            return retry ? MessageHandleResult.retry(summary(failure)) : MessageHandleResult.discard(summary(failure));
        }
    }

    private MessageHandleResult releaseThen(MessageIdempotencyKey key, String claimId, MessageHandleResult result) {
        if (idempotencyStore != null && !idempotencyStore.release(key, claimId)) {
            return MessageHandleResult.retry("Idempotency claim ownership was lost before release");
        }
        return result;
    }

    private void releaseAfterFailure(MessageIdempotencyKey key, String claimId, RuntimeException failure) {
        if (idempotencyStore == null || claimId == null) return;
        try {
            idempotencyStore.release(key, claimId);
        } catch (RuntimeException releaseFailure) {
            failure.addSuppressed(releaseFailure);
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value.trim();
    }

    private static Duration requirePositive(Duration value) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException("idempotencyLease must be positive");
        }
        return value;
    }

    private static String summary(Throwable failure) {
        return failure.getClass().getSimpleName() + (failure.getMessage() == null ? "" : ": " + failure.getMessage());
    }
}
