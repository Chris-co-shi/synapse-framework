package com.indigo.synapse.messaging.core;

/**
 * Broker 中立的逻辑目的地。
 *
 * @param name Spring Cloud Stream binding 名或由自定义 Transport 解释的逻辑名称
 * @param routingKey 可选路由键；具体含义由 Transport 映射，不承诺 Broker 专有语义
 */
public record MessageDestination(String name, String routingKey) {
    public MessageDestination {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        name = name.trim();
        routingKey = routingKey == null || routingKey.isBlank() ? null : routingKey.trim();
    }

    public static MessageDestination of(String name) {
        return new MessageDestination(name, null);
    }
}
