package com.indigo.synapse.messaging.reliability;

import com.indigo.synapse.messaging.core.MessageEnvelope;

/** 消费异常的重试决策端口。 */
@FunctionalInterface
public interface MessageRetryPolicy {
    boolean shouldRetry(MessageEnvelope envelope, Throwable failure, int attempt);
}
