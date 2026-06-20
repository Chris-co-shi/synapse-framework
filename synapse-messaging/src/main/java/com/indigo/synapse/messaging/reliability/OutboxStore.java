package com.indigo.synapse.messaging.reliability;

import com.indigo.synapse.messaging.core.MessageEnvelope;

/**
 * 发送方本地 Outbox 存储端口。
 *
 * <p>实现必须复用业务服务当前本地事务和同一数据源；Framework 不提供任何存储实现。</p>
 */
@FunctionalInterface
public interface OutboxStore {
    void append(MessageEnvelope envelope);
}
