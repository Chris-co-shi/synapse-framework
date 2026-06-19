# ADR-009：事务与数据库迁移边界

## Status

Accepted

## Context

通用事务注解和 Framework 内 migration 模块容易掩盖实际服务的数据所有权，并让基础 JAR
在消费方环境中自动改变 Schema。

## Decision

不创建 `synapse-transaction`、`synapse-migration`、`synapse-flyway` 或
`@SynapseTransactional`。应用使用 Spring `@Transactional`。数据库 Schema 由拥有数据的
可启动服务维护，Platform 默认采用 Flyway，但 Framework 不绑定 Flyway、不携带业务建表
脚本。本地事务加 Outbox 是默认最终一致性方案，Seata 仅由具体应用按需引入。

## Consequences

数据所有权明确，Framework JAR 不产生隐式 DDL。各服务必须自行管理 migration 生命周期和
多数据源 migration 配置。

## Rejected Alternatives

- Framework 自动执行 migration：无法确定业务 Schema 所有权。
- 自定义事务注解：重复 Spring 语义并隐藏传播行为。
- 默认分布式事务协调器：增加强耦合和运维成本。

## Date

2026-06-19
