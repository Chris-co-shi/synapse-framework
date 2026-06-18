# synapse-oauth2-resource-server-webflux 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webflux` 提供 Reactive OAuth2 Resource Server 技术适配。

当前能力：

- `ReactiveTokenDenylistPort`
- `SpringJwtClaimAccessor`
- `SynapseReactiveJwtAuthenticationConverter`
- `SynapseReactiveSecurityContext`
- `SynapseReactiveOperationContext`
- `SynapseReactiveSecurityContextWebFilter`
- reactive 401/403 `Result` 写出
- 默认 `SecurityWebFilterChain`

## 2. 主体与 Claim 规则

- `principal_type=USER` 映射为 `AuthenticatedUser`，用户标识来自 `sub`。
- `principal_type=CLIENT` 映射为 `AuthenticatedClient`，客户端标识来自 `client_id`。
- 主体类型使用 Core 的 `SynapsePrincipalType` 协议值判断，未知类型直接拒绝。
- `SpringJwtClaimAccessor` 只读取原始 claim；必填校验和集合规范化统一委托给 Core 的 `JwtClaimValues`。
- roles、permissions、scope 会过滤空白、按首次出现顺序去重。
- authority 固定按 scope、roles、permissions 顺序生成，并分别使用 `SCOPE_`、`ROLE_`、`PERM_` 前缀。
- 已带有对应前缀的值不会重复添加前缀。

上述规则必须与 Servlet WebMVC Resource Server 保持一致。

## 3. Reactor Context

WebFlux 场景通过 Reactor Context 读取：

```java
SynapseReactiveSecurityContext.currentPrincipal();
SynapseReactiveSecurityContext.currentUser();
SynapseReactiveSecurityContext.currentClient();
SynapseReactiveOperationContext.currentOperationContext();
```

不得依赖 Servlet ThreadLocal 作为唯一上下文。

## 4. 边界

该模块不是 Gateway 服务，不提供 RouteLocator、Gateway Filter 业务逻辑、网关鉴权后台或启动服务，也不创建私钥、`RSAKey` 或 `JwtEncoder`。
