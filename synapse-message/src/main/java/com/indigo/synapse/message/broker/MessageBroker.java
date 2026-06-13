package com.indigo.synapse.message.broker;

import java.util.Map;

/**
 * 消息中间件描述契约。
 *
 * <p>该模型只表达 broker 描述信息，不保存连接地址或凭据配置，也不负责连接 broker 或发送消息。</p>
 */
public record MessageBroker(
        String brokerCode,
        MessageBrokerType brokerType,
        String provider,
        String name,
        Map<String, String> attributes
) {

    public MessageBroker {
        if (brokerCode == null || brokerCode.isBlank()) {
            throw new IllegalArgumentException("brokerCode must not be blank");
        }
        if (brokerType == null) {
            throw new IllegalArgumentException("brokerType must not be null");
        }
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
