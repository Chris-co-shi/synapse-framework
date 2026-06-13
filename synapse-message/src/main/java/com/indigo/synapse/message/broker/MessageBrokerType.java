package com.indigo.synapse.message.broker;

/**
 * 消息中间件或消息传输基础设施类型。
 */
public enum MessageBrokerType {

    ROCKETMQ,
    KAFKA,
    RABBITMQ,
    REDIS_STREAM,
    PULSAR,
    CUSTOM
}
