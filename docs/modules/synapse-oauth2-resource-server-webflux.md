# synapse-oauth2-resource-server-webflux 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webflux` 提供 Reactive OAuth2 Resource Server 技术适配。

当前能力：

- `ReactiveTokenDenylistPort`
- `SynapseReactiveJwtAuthenticationConverter`
- `SynapseReactiveSecurityContext`
- `SynapseReactiveOperationContext`
- `SynapseReactiveSecurityContextWebFilter`
- reactive 401/403 `Result` 写出
- 默认 `SecurityWebFilterChain`

## 2. Reactor Context

WebFlux 场景通过 Reactor Context 读取：

```java
SynapseReactiveSecurityContext.currentPrincipal();
SynapseReactiveSecurityContext.currentUser();
SynapseReactiveSecurityContext.currentClient();
SynapseReactiveOperationContext.currentOperationContext();
```

不得依赖 Servlet ThreadLocal 作为唯一上下文。

## 3. 边界

该模块不是 Gateway 服务，不提供 RouteLocator、Gateway Filter 业务逻辑、网关鉴权后台或启动服务。
