# synapse-mq Skill

## 职责

`synapse-mq` 只提供 MQ 技术抽象、消息外壳、发布/消费 SPI、上下文传播、异常分类、幂等检查端口和自动配置。

## 禁止事项

- 不做 message-service。
- 不做站内信、短信、邮件、消息模板管理。
- 不做消息记录查询 API 或消息中心后台。
- 不绑定 RocketMQ、Kafka、RabbitMQ 等具体 SDK 到当前核心模块。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 发布端通过 `MessagePublishTemplate`。
- 消费端通过 `MessageConsumeTemplate`。
- 上下文传播通过 `OperationContextMessagePropagator`。
- 消息 header 名称保持 `MessageContextHeaders` 契约。
- 缺少 actor type 或 actor id 时不恢复默认身份。

## 测试要求

- 覆盖发布上下文写入。
- 覆盖消费上下文恢复和清理。
- 覆盖无上下文不伪造身份。
- 覆盖异常分类和幂等端口默认行为。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-mq.md`
- `docs/phase-2/03-boundary-checklist.md`
