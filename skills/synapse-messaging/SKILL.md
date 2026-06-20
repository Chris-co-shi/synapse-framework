# synapse-messaging Skill

## 职责

`synapse-messaging` 是单一 JAR 的 Broker 中立消息技术模块。使用 `MessageEnvelope`、
`MessageTransport`、发布器、Handler Dispatcher 和可靠性 SPI，不创建消息平台服务。

## 发布选择

- 可接受进程故障时消息丢失：使用 `BestEffortMessagePublisher`。
- 业务数据和消息必须最终一致：在同一本地事务中使用 `ReliableMessagePublisher`，并提供本地 `OutboxStore`。
- 可靠发布只登记 Outbox，不同步等待 Broker；无活动事务会失败。

## 消费规则

- Handler 的 `messageType` 必须唯一且稳定。
- At-least-once 允许重复，优先按 `eventId`、否则按 `messageId` 幂等。
- 应用必须提供持久化的 `MessageIdempotencyStore` 才能获得生产级幂等。
- Retry 和最终失败分别通过 `MessageRetryPolicy`、`MessageFailureStore` 扩展。

## Spring Cloud Stream

- 默认适配只在 `StreamBridge` 存在时创建。
- 用户 `MessageTransport` 优先。
- 应用负责引入 Binder 和配置 binding、consumer group、DLQ。
- Framework 不暴露 Broker 原生类型。

## 禁止事项

- 不拆 API/Core/Outbox/Starter。
- 不实现 JDBC、Redis、MongoDB 或文件 Outbox。
- 不创建中央 Outbox、消息服务、Controller、业务表、starter 或 demo。
- 不承诺 Exactly-once，不静默降级到本地文件。

## 测试要求

- 覆盖 Mock Transport 发布和上下文传播。
- 覆盖可靠发布事务前置条件与 Outbox 登记。
- 覆盖 Handler 路由、重复检测、Retry 和失败记录。
- 覆盖缺少 StreamBridge、用户 Transport、可靠配置缺少 OutboxStore 和 metadata。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-messaging.md`
- `docs/phase-2/03-boundary-checklist.md`
