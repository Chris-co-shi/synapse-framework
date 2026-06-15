# synapse-mq 使用手册

## 1. 模块定位

`synapse-mq` 是 Synapse Framework 的消息基础设施契约模块。

它负责定义通用 MQ 消息外壳、消息发布/消费 SPI、发布/消费模板、消息上下文传播、异常分类、幂等检查契约和自动配置入口。

该模块不等同于业务消息中心，也不表达站内信、短信、邮件、模板消息、已读未读等业务语义。
它不引入 RocketMQ / Kafka / RabbitMQ 等真实 MQ SDK，不实现 Redis 幂等，不做数据库消息表、Outbox 或事务消息。

## 2. 适用场景

适合以下场景：

- 业务模块需要统一构造 MQ 消息外壳。
- 生产者需要将 `OperationContext` 写入消息 header。
- 消费者需要从消息 header 恢复 `OperationContext`。
- 消费者需要把处理异常归类为 `SUCCESS` / `RETRY` / `DISCARD`。
- 后续 RocketMQ / Kafka / RabbitMQ 适配器需要统一发布和消费契约。

## 3. 不适用场景

不适合以下场景：

- 站内信中心。
- 短信、邮件、钉钉、企业微信等通知渠道实现。
- 消息模板管理。
- 消息已读未读。
- 消息中心后台。
- MQ Broker 运维管理。
- Redis 幂等默认实现。
- 数据库消息表、Outbox、事务消息或死信表。

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
- `MessageHeaderKeys`：消息自身元数据 header key。
- `MessagePublisher`：消息发布 SPI。
- `MessagePublishTemplate`：发布侧标准入口，发布前自动补充上下文 header。
- `MessageHandler`：消息消费处理 SPI。
- `MessageConsumeTemplate`：消费侧标准入口，消费前恢复上下文并分类异常。
- `MessagePublishResult`：消息发布结果。
- `MessageConsumeResult`：消息消费结果。
- `MessageErrorCode`：MQ framework 层技术错误码。
- `MessageException`：MQ 模块基础异常。
- `MessageExceptionClassifier`：消费异常到消费决策的分类接口。
- `MessageIdempotencyChecker`：消费幂等检查契约。
- `MessageContextHeaders`：消息上下文 header key。
- `OperationContextMessageCodec`：`OperationContextSnapshot` 与 header 的编解码。
- `OperationContextMessagePropagator`：生产和消费两端的上下文传播入口。

## 6. 快速使用示例

生产端推荐通过 `MessagePublishTemplate` 发布。模板会通过 `OperationContextProvider` 读取当前上下文，
并在发布前补充消息 header：

```java
MessageEnvelope envelope = MessageEnvelope.create(
        "sample.created",
        "sample-topic",
        "sample-tag",
        "sample-key",
        "sample-idempotent-key",
        "sample-service",
        "application/json",
        "v1",
        Map.of(),
        payload,
        traceId,
        tenantId,
        occurredAt
);

publishTemplate.publish(envelope);
```

消费端推荐通过 `MessageConsumeTemplate` 执行业务处理。模板会从消息 header 恢复上下文，
并把异常归类为未来 MQ 适配器可识别的消费决策：

```java
MessageConsumeResult result = consumeTemplate.consume(envelope, messageHandler);
```

未来 MQ 适配器可以将结果映射为：

```text
SUCCESS -> ACK / CONSUME_SUCCESS
RETRY   -> RECONSUME / retry
DISCARD -> ACK 后记录、告警、死信或丢弃
```

## 7. MQ 异常抽象与分类

`synapse-mq` 定义了 MQ framework 层异常体系，用于稳定表达消息外壳、header、payload、上下文传播、
发布、消费、路由和幂等处理等技术失败。

核心类型：

- `MessageErrorCode`：MQ 技术错误码枚举，错误码字符串发布后应保持稳定。
- `MessageException`：MQ 基础异常，继承 core 的 `SynapseException`。
- `MessageException.retryable()`：告诉消费异常分类器是否建议重试。
- `MessageValidationException`：消息结构、消息头或消息体约束失败，默认不可重试。
- `MessageSerializationException`：序列化或反序列化失败，默认不可重试。
- `MessagePublishException`：发布动作失败，默认可重试。
- `MessageConsumeException`：消费处理失败，是否重试由调用方指定。
- `MessageRoutingException`：topic、tag、key、messageType 等路由元数据解析失败，默认不可重试。
- `MessageContextPropagationException`：`OperationContext` 写入或恢复失败，默认不可重试。
- `MessageIdempotencyException`：幂等检查或标记失败，是否重试由调用方指定。

默认分类规则：

```text
null                                  -> RETRY
MessageException(retryable = true)    -> RETRY
MessageException(retryable = false)   -> DISCARD
IllegalArgumentException              -> DISCARD
其他异常                              -> RETRY
```

`DefaultMessageExceptionClassifier` 会把异常转换为 `MessageConsumeResult`，reason 只包含异常类名和 message，
不包含完整堆栈。未来 MQ adapter 可以把 Broker 原生异常转换为 `MessageException` 或其子类，再交给模板统一分类。

边界要求：

- 不允许把 RocketMQ / Kafka / RabbitMQ 原生异常暴露为 framework 对外契约。
- 不允许把业务异常或业务错误码塞进 `MessageErrorCode`。
- 不允许在 `synapse-mq` 中引入真实 Broker SDK、Redis 或数据库来实现异常处理。

## 8. 扩展方式

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

真实幂等检查由消费方或后续适配模块通过 `MessageIdempotencyChecker` 替换默认实现。
`NoopMessageIdempotencyChecker` 只是不保存状态的默认实现，不提供生产级幂等保护。

## 9. 配置项

当前模块没有外部配置项。

自动配置类：

```text
com.indigo.synapse.mq.autoconfigure.SynapseMqAutoConfiguration
```

默认装配：

- `OperationContextProvider`
- `OperationContextMessageCodec`
- `OperationContextMessagePropagator`
- `MessageExceptionClassifier`
- `MessageIdempotencyChecker`
- `MessageConsumeTemplate`

当应用中存在 `MessagePublisher` Bean 时，自动装配：

- `MessagePublishTemplate`

消费方可以声明同类型 Bean 覆盖默认实现。

## 10. 边界与注意事项

- `synapse-mq` 只依赖 `synapse-core`。
- 不直接依赖 `synapse-webmvc`、`synapse-security`、`synapse-data`、`synapse-audit`、`synapse-cache`、`synapse-file`。
- 不传播角色、权限和业务字段。
- 发布端读取当前上下文应通过 `OperationContextProvider`，不要直接散落调用 `OperationContextHolder.snapshot()`。
- 没有上下文的 MQ / Task / Async 场景应由调用方显式建立上下文。
- 不要把通知中心能力塞回 `synapse-mq`。

## 11. 常见问题

### 为什么不叫 synapse-message？

因为 `message` 容易和站内信、短信、邮件、模板消息等业务消息中心混淆。

Framework 层的职责是 MQ / Messaging 技术契约，因此使用 `synapse-mq` 更清晰。

### 现在是否已经支持 RocketMQ？

当前只定义通用契约和上下文传播，还没有绑定 RocketMQ SDK。

RocketMQ 适配器应作为后续能力追加，不能污染当前基础契约。
