package com.indigo.synapse.message.core;

/**
 * 可靠消息状态。
 */
public enum ReliableMessageStatus {

    PENDING,
    SENDING,
    SENT,
    RETRY,
    DLQ,
    CANCELLED,
    COMPENSATING,
    COMPENSATED
}
