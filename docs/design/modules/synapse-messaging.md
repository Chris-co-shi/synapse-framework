# synapse-messaging 设计说明

## 1. 决策

`synapse-messaging` 保持单一 JAR，以 Broker 中立 Port 隔离业务消息语义与传输实现。
默认传输适配选择 Spring Cloud Stream `StreamBridge`，但 Stream 依赖为 optional，且不引入具体 Binder。

## 2. 模型

```text
MessageEnvelope
  -> MessageMetadata(messageId, eventId, messageType, source, version, headers, time)
  -> MessageDestination(logical binding, optional routing key)
  -> serialized payload
```

Envelope 不出现 Kafka、RocketMQ、RabbitMQ 原生类型。`eventId` 是领域事件稳定身份，
`messageId` 是消息实例身份。

## 3. 发布链路

```text
Best effort:
MessageEnvelope -> context propagation -> MessageTransport -> Broker

Reliable:
active local transaction -> context propagation -> local OutboxStore.append
```

可靠发布不等待 Broker，不调用中央服务。Outbox 实现由发送方服务提供，并与业务数据处于同一数据源、
同一本地事务。无活动事务时明确失败。

## 4. 消费链路

```text
MessageEnvelope
  -> MessageDispatcher
  -> restore OperationContext
  -> MessageHandlerRegistry
  -> MessageHandler
  -> MessageHandleResult
```

可选的 `MessageIdempotencyStore`、`MessageRetryPolicy`、`MessageFailureStore` 分别负责幂等、重试决策和
最终失败记录。Handler 成功后才标记幂等键；优先使用 eventId，否则使用 messageId。

## 5. 一致性边界

- 可靠链路是 At-least-once，重复是正常行为。
- Framework 不承诺 Exactly-once。
- Framework 不实现 Outbox/Inbox 表、扫描器、Scheduler、集群抢占或持久化适配。
- 并发幂等和业务事务原子性由应用的 Store 实现负责。
- 不允许静默降级到文件存储。

## 6. 自动配置

- 基础配置不依赖 StreamBridge 类加载。
- `StreamBridge` 类和 Bean 同时存在时才创建默认 Transport。
- 用户 `MessageTransport` 优先。
- 显式开启 reliable 但缺少唯一 `OutboxStore` 时启动失败。
- `synapse.messaging.enabled=false` 关闭模块自动配置。

## 7. 上下文与安全

只传播 actor、initiator、source、traceId、requestId、tenantId 等技术上下文。
禁止传播 raw token、password、roles、permissions、Cookie 或大段敏感 payload。

## 8. 修改检查

- 是否泄漏 Broker 专有类型或引入具体 Binder。
- 是否新增 JDBC、Redis、MongoDB、文件 Store 实现。
- 是否把可靠登记误写成 Broker 已确认或 Exactly-once。
- 是否在无事务时允许可靠发布。
- 是否让默认 Transport 覆盖用户实现。
- 是否创建中央 Outbox 或同步调用 Platform 消息服务。
