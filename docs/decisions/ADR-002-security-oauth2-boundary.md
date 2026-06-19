# ADR-002：Security 与 OAuth2 边界

## Status

Accepted

## Context

通用主体上下文与 OAuth2 Resource Server 适配存在名称混淆和共享验证语义分散问题。
Reactive 链路也不能把 ThreadLocal 作为统一上下文来源。

## Decision

`synapse-security` 保持协议中立，只定义认证主体、权限检查和当前主体上下文。
上下文类型采用 `CurrentPrincipalContext`、`PrincipalContextState`、
`PrincipalContextBinder`、`PrincipalContextScope`。Servlet 严格管理 ThreadLocal
生命周期，WebFlux 使用 Reactor Context。OAuth2 按 core、authorization support、
client、resource-server core、MVC/WebFlux adapter 分层。

## Consequences

Security 不依赖 OAuth2；Resource Server 可统一 issuer、audience、claims、token type、
denylist 与主体映射语义。公开类型重命名会产生源码兼容影响。

## Rejected Alternatives

- 在 Security 中直接实现 OAuth2：协议和通用主体模型耦合。
- Reactive 继续依赖 ThreadLocal：线程切换后存在串线和丢失风险。
- MVC/WebFlux 各自维护验证规则：长期产生语义漂移。

## Date

2026-06-19
