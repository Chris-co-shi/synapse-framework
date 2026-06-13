# Redis Pub/Sub 消息引用操作文档

本文档说明 `synapse-message` 提供的 Redis Pub/Sub 在线广播 adapter。该能力默认关闭，只有配置显式开启且 Redis Bean 满足条件时才注册。

## 1. 启用配置

```yaml
synapse:
  message:
    redis:
      pubsub:
        enabled: true
```

启用条件：

- classpath 中存在 Spring Data Redis。
- Spring 容器中存在 `StringRedisTemplate`。
- Spring 容器中存在 `RedisConnectionFactory`。
- 未被消费方自定义 `MessagePublisher` 或 `MessageSubscriber` 覆盖。

## 2. 可注入 Bean

```java
MessagePublisher messagePublisher;
MessageSubscriber messageSubscriber;
```

默认关闭时不会注册上述 Redis Pub/Sub Bean。

## 3. 消息外壳

`MessageEnvelope` 是框架通用消息外壳：

```text
messageId
topic
tag
key
headers
payload
traceId
tenantId
createdAt
```

约束：

- `topic` 由消费方定义，框架不内置业务 topic。
- `payload` 是字符串，由消费方定义格式。
- `headers` 只承载技术元数据或消费方自定义元数据。

## 4. 发布消息

```java
MessageEnvelope message = MessageEnvelope.create(
        "synapse:demo:event",
        "tag",
        "key-1",
        Map.of("x-trace-id", "trace-1"),
        "{\"value\":\"hello\"}",
        "trace-1",
        "tenant-1"
);

MessagePublishResult result = messagePublisher.publish(message);
```

`subscriberCount` 是 Redis 返回的当前在线订阅者数量，不代表可靠送达确认。

## 5. 订阅和取消订阅

```java
messageSubscriber.subscribe("synapse:demo:event", message -> {
    String payload = message.payload();
});

messageSubscriber.unsubscribe("synapse:demo:event");
```

订阅关系只在当前 JVM 内生效，进程退出后不会保留。

## 6. 可靠性边界

Redis Pub/Sub 是在线广播能力：

- 不持久化消息。
- 不支持离线消费。
- 不提供消费确认。
- 不提供重试、死信或补偿。
- 不保证业务可靠投递。

需要可靠业务消息时，应在 `synapse-message` 后续接入 Outbox、Retry、Dead Letter、Compensation 或外部 MQ adapter，不应依赖 Redis Pub/Sub。

## 7. 与 cache 模块边界

- `synapse-cache` 负责缓存、Redis 数据结构、锁、限流和幂等。
- `synapse-message` 负责消息外壳、发布订阅端口和消息 adapter。
- Redis Pub/Sub adapter 放在 `synapse-message`，通过配置显式启用。
