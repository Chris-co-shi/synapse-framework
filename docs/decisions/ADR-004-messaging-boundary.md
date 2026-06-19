# ADR-004：Messaging 边界

## Status

Accepted

## Context

`synapse-mq` 名称偏向具体中间件，现有抽象尚未完整覆盖 handler dispatch、本地 Outbox
登记、幂等、失败存储、重试编排和 Spring Cloud Stream 条件适配。

## Decision

将模块更名为单一 JAR `synapse-messaging`。它保持 Broker 中立，提供 envelope、发布/
消费抽象、handler dispatch、可选 Stream 适配及可靠性 SPI。Reliable Publisher 仅在
活动本地事务中登记发送方本地 Outbox，无事务明确失败；可靠消息语义为 at-least-once，
消费方按消息标识幂等。

## Consequences

artifactId、包名、自动配置和文档需要同步迁移。Framework 不提供任何 JDBC、Redis、
MongoDB 或文件 Outbox 实现，也不承诺通用 exactly-once。

## Rejected Alternatives

- 拆分 api/core/outbox/starter：当前规模下增加发布和消费复杂度。
- 中央 Outbox 服务：破坏本地事务原子性。
- 文件存储降级：无法提供可靠事务语义。
- 绑定 RocketMQ/Kafka API：破坏 Broker 中立。

## Date

2026-06-19
