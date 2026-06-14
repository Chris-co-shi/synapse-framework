# synapse-mq 使用手册

## 1. 模块定位

`synapse-mq` 是 Synapse Framework 的消息基础设施契约模块。

它负责定义通用消息外壳、消息发布/消费 SPI、消息上下文传播和自动配置入口。

该模块不等同于业务消息中心，也不表达站内信、短信、邮件、模板消息、已读未读等业务语义。

## 2. 适用场景

适合以下场景：

- 业务模块需要统一构造 MQ 消息外壳。
- 生产者需要将 `OperationContext` 写入消息 header。
- 消费者需要从消息 header 恢复 `OperationContext`。
- 后续 RocketMQ / Kafka / RabbitMQ 适配器需要统一发布和消费契约。

## 3. 不适用场景

不适合以下场景：

- 站内信中心。
- 短信、邮件、钉钉、企业微信等通知渠道实现。
- 消息模板管理。
- 消息已读未读。
- 消息中心后台。
- MQ Broker 运维管理。

## 4. Maven 引入方式

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-mq</artifactId>
</dependency>
```

建议通过 `synapse-bom` 统一管理版本。

## 5. 核心能力

当前核心能力包括：

- `MessageEnvelope`：通用消息外壳。
- `MessagePublisher`：消息发布 SPI。
- `MessageHandler`：消息消费处理 SPI。
- `MessagePublishResult`：消息发布结果。
- `MessageConsumeResult`：消息消费结果。
- `MessageContextHeaders`：消息上下文 header key。
- `OperationContextMessageCodec`：`OperationContextSnapshot` 与 header 的编解码。
- `OperationContextMessagePropagator`：生产和消费两端的上下文传播入口。

## 6. 快速使用示例

生产端可以在发布前补充当前上下文：

```java
MessageEnvelope envelope = MessageEnvelope.create(
        "user-topic",
        "created",
        userId,
        Map.of(),
        payload,
        traceId,
        tenantId
);

MessageEnvelope enriched = propagator.withCurrentContext(envelope);
messagePublisher.publish(enriched);
```

消费端可以在业务处理前恢复上下文：

```java
try (OperationContextScope ignored = propagator.restore(envelope)) {
    messageHandler.handle(envelope);
}
```

## 7. 扩展方式

具体 MQ SDK 不直接放入当前基础契约。

后续可以通过适配器扩展：

```text
RocketMQ Listener / Producer
  -> MessageEnvelope
  -> MessagePublisher / MessageHandler
```

适配器负责处理：

- SDK 消息对象转换。
- 发布确认。
- 消费确认。
- 重试。
- 死信。
- 顺序消息。
- 延迟消息。

## 8. 配置项

当前模块没有外部配置项。

自动配置类：

```text
com.indigo.synapse.mq.autoconfigure.SynapseMqAutoConfiguration
```

默认装配：

- `OperationContextMessageCodec`
- `OperationContextMessagePropagator`

消费方可以声明同类型 Bean 覆盖默认实现。

## 9. 边界与注意事项

- `synapse-mq` 只依赖 `synapse-core`。
- 不直接依赖 `synapse-web`、`synapse-security`、`synapse-data`、`synapse-audit`、`synapse-cache`、`synapse-file`。
- 不传播角色、权限和业务字段。
- 没有上下文的 MQ / Task / Async 场景应由调用方显式建立上下文。
- 不要把通知中心能力塞回 `synapse-mq`。

## 10. 常见问题

### 为什么不叫 synapse-message？

因为 `message` 容易和站内信、短信、邮件、模板消息等业务消息中心混淆。

Framework 层的职责是 MQ / Messaging 技术契约，因此使用 `synapse-mq` 更清晰。

### 现在是否已经支持 RocketMQ？

当前只定义通用契约和上下文传播，还没有绑定 RocketMQ SDK。

RocketMQ 适配器应作为后续能力追加，不能污染当前基础契约。
