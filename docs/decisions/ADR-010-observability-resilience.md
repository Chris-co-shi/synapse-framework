# ADR-010：Observability 与 Resilience

## Status

Accepted

## Context

Framework 的关键链路需要统一观测和韧性语义，但不应自研 tracing、绑定具体 APM，或把
特定限流/熔断产品强加给所有应用。

## Decision

新增 `synapse-observability`，基于 Micrometer Observation 提供命名、低基数 tag、
Trace/MDC 桥接和健康扩展约定；不绑定 Prometheus、Zipkin、Tempo、SkyWalking。
新增 `synapse-resilience`，基于 Resilience4j、Spring Cloud CircuitBreaker 和 Spring
官方机制提供 timeout、retry、circuit breaker、bulkhead、异常分类与 observation。
非幂等操作默认不重试，不提供返回假成功数据的通用 fallback，不依赖 Sentinel。

## Consequences

各模块获得统一且供应商中立的扩展点。消费方仍需选择 registry、exporter、tracer 和具体
resilience 实现；标签设计必须避免 URL、用户 ID、token、SQL 等高基数或敏感值。

## Rejected Alternatives

- 自研 tracer：重复标准生态且难以互操作。
- 绑定具体 APM/exporter：限制部署选择。
- 默认积极重试：可能放大故障或重复非幂等副作用。
- Sentinel：与已确认的依赖边界不符。

## Date

2026-06-19
