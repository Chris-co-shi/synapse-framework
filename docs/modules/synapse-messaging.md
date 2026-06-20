# synapse-messaging 使用手册

## 1. 模块定位

`synapse-messaging` 是单一 JAR 的 Broker 中立消息基础模块，提供消息模型、发布/消费编排、
Spring Cloud Stream 可选适配、上下文传播及可靠性 SPI。它不是消息中心，也不创建中央 Outbox 服务。

模块不拆分 API/Core/Outbox/Starter，不引入 Kafka、RocketMQ、RabbitMQ Binder 或原生 SDK，
不实现 JDBC、Redis、MongoDB、文件 Outbox 和幂等存储。

## 2. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-messaging</artifactId>
</dependency>
```

`spring-cloud-stream` 是 optional 依赖。应用需要默认传输适配时自行引入 Spring Cloud Stream 和具体 Binder；
缺少 Spring Cloud Stream 或 `StreamBridge` 时，Messaging 基础自动配置仍可正常启动。

## 3. 核心模型

- `MessageEnvelope`：组合 Metadata、Destination 和已序列化 payload。
- `MessageMetadata`：messageId、eventId、messageType、来源、内容类型、版本、header 和时间。
- `MessageDestination`：逻辑 binding 名和可选路由键，不表达 Broker 专有概念。
- `MessageVersion`：负载结构版本。
- `MessagePublishResult`：`SENT`、`STORED` 或 `FAILED`。
- `MessageHandleResult`：`SUCCESS`、`DUPLICATE`、`RETRY` 或 `DISCARD`。

`eventId` 表示同一领域事件的稳定身份，重投时应保持不变；`messageId` 表示消息实例。
消费幂等键优先使用 `eventId`，缺失时回退到 `messageId`。

## 4. 发布语义

`BestEffortMessagePublisher` 通过 `MessageTransport` 立即发送，不持久化失败消息。

`ReliableMessagePublisher` 只在当前活动本地事务中调用 `OutboxStore.append`：

- 无活动本地事务时抛出明确异常。
- 只登记发送方服务的本地 Outbox，不同步等待 Broker。
- `OutboxStore` 实现必须与业务数据使用同一数据源和同一本地事务。
- Framework 不提供 Outbox 表、实现、扫描器、Scheduler 或集群抢占。

可靠链路采用 At-least-once，允许消息重复，不承诺通用 Exactly-once。

## 5. 消费语义

应用通过 `MessageHandler.messageType()` 声明路由类型。`MessageHandlerRegistry` 拒绝重复类型，
`MessageDispatcher` 负责上下文恢复、Handler 查找、幂等检查、结果处理和失败决策。

可选 SPI：

- `MessageIdempotencyStore`：查询和标记 `messageId/eventId`。
- `MessageRetryPolicy`：按异常和尝试次数决定是否重试。
- `MessageFailureStore`：记录最终失败摘要，不含 payload 和异常堆栈。

Framework 不提供这些 SPI 的生产实现。若未提供 `MessageIdempotencyStore`，Dispatcher 不会伪装成已具备幂等保护。

## 6. Spring Cloud Stream

`SpringStreamMessageTransport` 使用 `StreamBridge` 将逻辑目的地解释为 binding 名。
仅当 `StreamBridge` 类和 Bean 都存在时才创建默认 `MessageTransport`；用户自定义 `MessageTransport`
始终优先。具体 Binder、destination、consumer group、重试和 DLQ 由应用配置。

## 7. 配置

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.messaging.enabled` | `true` | 启用 Messaging 基础自动配置 |
| `synapse.messaging.stream.enabled` | `true` | 允许创建默认 StreamBridge 传输 |
| `synapse.messaging.reliable.enabled` | `false` | 创建可靠发布器；开启后必须存在唯一 `OutboxStore` Bean |

显式开启可靠发布但缺少 `OutboxStore` 时，应用启动快速失败。配置 metadata 由
`spring-boot-configuration-processor` 自动生成。

## 8. 边界

- 不暴露 Broker 专有类型。
- 不同步调用 Platform 消息服务。
- 不传播角色、权限或业务身份 Header。
- 不实现通知、短信、邮件、站内信和模板业务。
- 不实现任何 Outbox、幂等或失败存储。
- 不创建 Controller、Entity、Repository、migration、starter 或示例应用。
