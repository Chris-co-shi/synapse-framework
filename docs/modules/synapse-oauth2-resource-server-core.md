# synapse-oauth2-resource-server-core 使用手册

## 模块定位

`synapse-oauth2-resource-server-core` 用于统一 MVC 与 WebFlux Resource Server 的验证语义。

## 当前事实

Phase 1 仅建立可编译 JAR 和包边界，issuer、audience、required claims、token type、
denylist 和 principal mapping 仍将在后续阶段迁移。

## 边界

- 不得包含 Servlet、Spring MVC、Reactor 或 WebFlux 类型。
- 不提供 JWT 签发、私钥、登录或 Authorization Server 能力。
