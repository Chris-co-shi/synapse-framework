# ADR-001：Web 模块结构

## Status

Accepted

## Context

MVC 与 WebFlux 当前是根级并列模块，缺少技术栈无关的响应、错误和 JSON 扩展层，
导致共享语义容易复制或反向依赖具体 Web 技术栈。

## Decision

建立 `synapse-web` 聚合 POM，下设 `synapse-web-core`、`synapse-webmvc` 和
`synapse-webflux`。应用只依赖具体适配模块。`web-core` 禁止 Servlet、MVC、
Reactor 和 WebFlux 类型。JSON 扩展使用 Jackson `Module` 与
`Jackson2ObjectMapperBuilderCustomizer`，Framework 不提供全局 `ObjectMapper`。

## Consequences

共享 Web 契约有稳定归属，MVC/WebFlux 可独立装配；模块迁移会改变源码路径和 Maven
坐标管理，需要同步 imports、测试与文档。

## Rejected Alternatives

- 保持两个模块完全独立：继续复制共享模型。
- 创建 starter 聚合依赖：违反 Framework 不提供 starter 的边界。
- 让一方依赖另一方：造成 Servlet 与 Reactive 技术栈污染。

## Date

2026-06-19
