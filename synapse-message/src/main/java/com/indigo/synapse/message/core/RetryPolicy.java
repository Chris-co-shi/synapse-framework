package com.indigo.synapse.message.core;

import java.time.Instant;

/**
 * 消息重试策略。
 */
public interface RetryPolicy {

    RetryDecision decide(ReliableMessage message, Throwable failure, Instant now);
}
