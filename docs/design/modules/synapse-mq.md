# synapse-mq 设计说明

## 1. 模块使命

`synapse-mq` 定义跨 Broker 的消息外壳、发布/消费 Port、上下文传播和消费结果分类，使 RocketMQ、Kafka、RabbitMQ 等 adapter 可以共享统一语义，而 Framework 核心不依赖任何真实 Broker SDK。

## 2. 边界

负责：

- `MessageEnvelope` 与标准 Header。
- `MessagePublisher`、`MessageHandler`。
- 发布与消费模板。
- OperationContext 消息 Header 编解码。
- 技术异常体系与 RETRY/DISCARD 分类。
- 幂等检查 Port 和 Noop 默认实现。
- 条件自动配置。

不负责：

- 站内信、短信、邮件和模板消息中心。
- Broker Producer/Listener SDK 实现。
- ACK、重试次数、死信和顺序消息的具体映射。
- Redis 幂等实现。
- Outbox、事务消息和数据库消息表。
- 业务异常码全集。

## 3. 消息外壳设计

`MessageEnvelope` 承载：

- messageType、topic、tag、key。
- idempotentKey、producer、contentType、schemaVersion。
- headers、payload。
- traceId、tenantId、occurredAt。

Envelope 是跨 adapter 契约，不应直接暴露 RocketMQ/Kafka 原生消息对象。

## 4. 发布链路

```text
business constructs MessageEnvelope
  -> MessagePublishTemplate
  -> OperationContextProvider
  -> OperationContextMessagePropagator
  -> enrich context headers
  -> MessagePublisher Port
  -> broker adapter
```

业务代码优先使用 template，而不是散落调用 Holder 或自己拼上下文 Header。

## 5. 消费链路

```text
broker adapter converts native message
  -> MessageEnvelope
  -> MessageConsumeTemplate
  -> idempotency checker
  -> restore OperationContext scope
  -> MessageHandler
  -> classify exception
  -> MessageConsumeResult SUCCESS / RETRY / DISCARD
  -> adapter maps result to ACK / retry / DLQ policy
```

Template 只给出技术建议，不直接控制 Broker。

## 6. 上下文传播边界

允许传播 actor、initiator、source、traceId、requestId 和 tenantId 承载位；禁止传播 raw token、password、roles、permissions 和大段业务上下文。

缺少 actor type/id 时不恢复伪上下文，也不默认创建 system actor。生产者或任务入口必须显式建立 OperationContext。

## 7. 异常分类设计

```text
MessageException retryable=true  -> RETRY
MessageException retryable=false -> DISCARD
IllegalArgumentException         -> DISCARD
unknown technical exception      -> RETRY
```

业务系统需要根据自身错误语义把异常转换为明确的 `MessageConsumeException`，不能把所有业务失败都无限重试。

reason 只保留有限类名与 message，不包含完整堆栈和敏感 payload。

## 8. 幂等边界

`MessageIdempotencyChecker` 是 Port。Noop 默认实现不保存状态，仅保证模块可装配；它不是生产级防重。

真实实现需要决定：

- 幂等 key 作用域。
- 何时标记处理中/成功。
- 业务失败是否释放。
- 并发消费与过期策略。
- 与事务提交的原子性。

这些通常由 adapter、业务状态表或 Outbox/Inbox 方案处理。

## 9. 可靠性边界

- `MessagePublisher` 成功不自动等于业务事务与消息原子提交。
- Template 不提供 Outbox。
- RETRY 只是建议，实际次数和延迟由 Broker adapter 决定。
- DISCARD 不等于静默丢弃，adapter 可以告警、记录或转死信。
- payload schemaVersion 必须由消息生产/消费双方治理。

## 10. 扩展原则

- RocketMQ/Kafka/RabbitMQ：单独 adapter 实现 Publisher 和 Listener 转换。
- Redis/DB 幂等：实现 `MessageIdempotencyChecker`。
- 可靠发布：业务或 Platform 使用 Outbox，再调用 Publisher。
- 新 Broker 异常先转换为 Framework `MessageException`，不要泄露 SDK 类型。

## 11. 源码阅读顺序

```text
MessageEnvelope / HeaderKeys
  -> MessagePublisher / MessagePublishResult
  -> OperationContextMessageCodec / Propagator
  -> MessagePublishTemplate
  -> MessageHandler / MessageConsumeResult
  -> MessageException hierarchy
  -> MessageExceptionClassifier
  -> MessageIdempotencyChecker
  -> MessageConsumeTemplate
  -> SynapseMqAutoConfiguration
  -> context cleanup and classification tests
```

## 12. 手写练习

1. USER OperationContext 下发布消息，验证 Header 包含 actor/trace 但不含 permissions/token。
2. 消费时恢复上下文并在 handler 中读取 actor。
3. handler 抛 retryable / non-retryable 异常，验证结果分类。
4. 用 Noop checker 重复消费，说明为什么它不防重。
5. 设计一个 Inbox 表方案解决消费幂等与业务事务原子性。

## 13. 修改检查清单

- 是否引入真实 Broker SDK、Redis 或数据库。
- 是否混入通知中心业务语义。
- 是否传播 token、权限或敏感 payload。
- 是否在无上下文时默认 system。
- 是否把 Noop 幂等描述为生产保护。
- 是否把 Template 描述成事务消息/Outbox。
- SDK 异常是否泄露到公共契约。
- 消费 scope 是否在异常后正确关闭。
