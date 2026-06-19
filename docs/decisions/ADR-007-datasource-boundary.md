# ADR-007：Datasource 边界

## Status

Accepted

## Context

Framework 需要统一多数据源定义、路由、刷新、健康和观测，但不应重新实现成熟的动态数据源
引擎或暗含跨库事务。

## Decision

保留单一 `synapse-datasource`，底层使用
`dynamic-datasource-spring-boot3-starter`。路由必须在事务开始前确定；一个本地事务绑定
一个数据源，活动事务中切换默认抛错。Framework 不实现跨库事务，不默认依赖 Seata，
不绑定 Nacos，也不重复代理 DataSource。

## Consequences

路由生命周期和事务边界可预测。业务若需要跨库一致性，应采用本地事务加 Outbox 或由具体
应用显式引入协调方案。

## Rejected Alternatives

- 自研动态数据源引擎：维护成本高且重复成熟能力。
- 事务中透明切库：破坏连接和事务一致性。
- 默认集成 Seata：把分布式事务策略强加给所有消费方。

## Date

2026-06-19
