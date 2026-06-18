/**
 * Broker 无关的 MQ 消息契约与发布/消费模板。
 *
 * <p>该模块定义 MessageEnvelope、Publisher/Handler Port、OperationContext 消息传播、消费异常分类和
 * 幂等检查扩展点，不引入 RocketMQ、Kafka、RabbitMQ、Redis 或数据库 SDK。</p>
 *
 * <p>Noop 幂等检查不提供生产级防重，发布模板不等于 Outbox 或事务消息，RETRY/DISCARD 只是供具体
 * Broker adapter 映射的技术决策。消息 Header 不得传播 raw token、密码、roles 或 permissions。</p>
 */
package com.indigo.synapse.mq;
