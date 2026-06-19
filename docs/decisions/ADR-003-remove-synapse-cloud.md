# ADR-003：删除 synapse-cloud

## Status

Accepted

## Context

`synapse-cloud` 包装 OpenFeign 上下文传播和错误解码，但其边界容易扩张为 Gateway、
注册中心或通用远程调用 SDK，并增加 Framework 对 Spring Cloud 的间接封装。

## Decision

删除 `synapse-cloud`，不创建 `synapse-cloud-openfeign`。应用直接使用 Spring Cloud
OpenFeign。可复用的 Bearer Token 出站能力归 `synapse-oauth2-client`；通用观测和
韧性能力分别归 observability 与 resilience。

## Consequences

Framework 模块边界收紧，消费方需要直接配置 OpenFeign；删除前必须审查现有代码，
将仍有价值且符合边界的契约迁移到明确模块。

## Rejected Alternatives

- 保留并继续扩展 cloud：职责过宽且与官方能力重复。
- 更名为 cloud-openfeign：仍然维持不必要包装。
- 将其变成 Gateway：Gateway 是 Platform 可启动服务。

## Date

2026-06-19
