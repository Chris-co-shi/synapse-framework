/**
 * Broker 中立的消息模型、发布/消费编排和可靠性扩展端口。
 *
 * <p>该模块定义 Envelope、Publisher、Transport、Handler Dispatcher、OperationContext 消息传播和
 * Outbox/幂等/失败存储 SPI，不引入具体 Binder、Broker、Redis 或数据库实现。</p>
 *
 * <p>可靠发布采用本地事务 Outbox 和 At-least-once 语义，允许重复且不承诺 Exactly-once。
 * 消息 Header 不得传播 raw token、密码、roles 或 permissions。</p>
 */
package com.indigo.synapse.messaging;
