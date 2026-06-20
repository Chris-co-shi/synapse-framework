package com.indigo.synapse.messaging.transport;

import com.indigo.synapse.messaging.core.MessageEnvelope;

/**
 * 消息传输端口。
 *
 * <p>实现只能报告传输层是否接受消息，不能把 Broker 原生结果或类型泄漏给调用方。</p>
 */
@FunctionalInterface
public interface MessageTransport {
    MessageTransportResult send(MessageEnvelope envelope);
}
