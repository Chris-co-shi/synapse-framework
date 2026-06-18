# synapse-oauth2-resource-server-webflux 设计说明

## 1. 模块使命

该模块把 Reactive OAuth2 Resource Server 的 JWT 认证结果适配为 Synapse 主体，并通过 Reactor Context 传播 SecurityContext 与 OperationContext。它可以被 Platform Gateway 引用，但自身不是 Gateway 服务。

## 2. 为什么与 Servlet 模块分离

Reactive 链路可能跨线程，Servlet ThreadLocal 不能作为唯一上下文。Reactive 模块必须使用 Reactor Context，并通过 `Mono` / `Flux` 生命周期传递身份。

## 3. 边界

负责：

- Reactive JWT Authentication converter。
- Reactive token denylist Port。
- USER / CLIENT 主体映射。
- `SynapseReactiveSecurityContext`。
- `SynapseReactiveOperationContext`。
- Security WebFilter 与 Reactor Context 写入。
- Reactive 401/403 Result 写出。
- 默认 `SecurityWebFilterChain` 技术配置。

不负责：

- Gateway RouteLocator 和业务 Filter。
- 网关权限后台与路由配置。
- JWT 签发和私钥。
- IAM 登录、用户与客户端管理。

## 4. 核心对象角色

- Reactive converter：`Jwt -> Mono<Authentication>`，可组合异步 denylist 检查。
- `SynapseReactiveSecurityContext`：从 Reactor Context 读取 principal/user/client。
- `SynapseReactiveOperationContext`：从相同主体生成或读取 OperationContext。
- `SynapseReactiveSecurityContextWebFilter`：在认证完成后的 publisher 链中写入 Context。
- Reactive handlers：以非阻塞方式写出 401/403 Result。

## 5. 主链路

```text
Bearer token
  -> AuthenticationWebFilter / ReactiveJwtDecoder
  -> validators
  -> SynapseReactiveJwtAuthenticationConverter
  -> Authentication in ReactiveSecurityContextHolder
  -> SynapseReactiveSecurityContextWebFilter
  -> contextWrite(principal + operation context)
  -> handler / gateway technical chain
```

读取必须发生在订阅链中：

```java
SynapseReactiveSecurityContext.currentUser()
    .flatMap(user -> ...);
```

不能在普通同步方法中假设 ThreadLocal 存在。

## 6. Reactive denylist

Reactive Port 允许非阻塞 Redis/远程检查。实现不得在 event-loop 中调用阻塞 JDBC 或同步 Redis 客户端；如不得不阻塞，应由 adapter 明确切换 scheduler，但更推荐真正 Reactive 实现。

## 7. 生命周期与失败边界

- 使用 `contextWrite` 建立下游可见的 Context。
- 不把主体保存为全局变量或静态 ThreadLocal。
- WebFilter 顺序必须在认证后建立 Synapse Context。
- response committed 后不得重复写 401/403。
- 错误消息不得泄露 token 和验证细节。
- Reactive 流取消也不能产生全局上下文残留，因为 Context 随订阅绑定。

## 8. 扩展原则

- Gateway 可以增加业务鉴权 Filter，但放在 Platform Gateway。
- 自定义 Reactive denylist 实现 OAuth2 core 语义的 reactive adapter。
- Servlet 与 Reactive 可以共享 claim 规则，但不要共享 ThreadLocal bridge 实现。
- 如提取共享 mapper，应保持它不依赖 Servlet/WebFlux。

## 9. 源码阅读顺序

```text
ReactiveTokenDenylistPort
  -> Reactive JWT converter
  -> Reactive authentication token / principal mapping
  -> SynapseReactiveSecurityContext
  -> SynapseReactiveOperationContext
  -> SynapseReactiveSecurityContextWebFilter
  -> 401 / 403 writers
  -> SecurityWebFilterChain auto configuration
  -> StepVerifier / WebTestClient tests
```

## 10. 手写练习

1. 在 Reactor Context 写入 USER principal。
2. `publishOn` 切线程后读取 currentUser。
3. 使用 fake reactive denylist 拒绝某个 jti。
4. 验证匿名、无权限分别返回 401 和 403。

## 11. 修改检查清单

- 是否使用阻塞调用运行在 event-loop。
- 是否把 ThreadLocal 当成主上下文。
- 是否把 Gateway 业务能力放进 Framework。
- 是否混用 Servlet Filter / `jakarta.servlet`。
- WebFilter 是否在认证前错误读取主体。
- Reactive Context 是否通过 publisher 链传播。
- 用户自定义 `SecurityWebFilterChain` 是否能覆盖默认配置。
