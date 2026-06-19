# synapse-oauth2-resource-server-webflux 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webflux` 提供 Reactive OAuth2 Resource Server 技术适配。

当前能力：

- `SynapseReactiveResourceServerProperties`
- `ReactiveTokenDenylistPort`
- `SpringJwtClaimAccessor`
- `SynapseReactiveJwtAuthenticationConverter`
- `SynapseReactiveSecurityContext`
- `SynapseReactiveOperationContext`
- `SynapseReactiveSecurityContextWebFilter`
- reactive 401/403 `Result` 写出
- 默认 `ReactiveJwtDecoder`
- 默认 `SecurityWebFilterChain`
- `SynapseResourceServerServerHttpSecurityConfigurer`

## 2. 主体与 Claim 规则

- `principal_type=USER` 映射为 `AuthenticatedUser`，用户标识来自 `sub`。
- `principal_type=CLIENT` 映射为 `AuthenticatedClient`，客户端标识来自 `client_id`。
- 主体类型使用 Core 的 `SynapsePrincipalType` 协议值判断，未知类型直接拒绝。
- `SpringJwtClaimAccessor` 只读取原始 claim；必填校验和集合规范化统一委托给 Core 的 `JwtClaimValues`。
- roles、permissions、scope 会过滤空白、按首次出现顺序去重。
- authority 固定按 scope、roles、permissions 顺序生成，并分别使用 `SCOPE_`、`ROLE_`、`PERM_` 前缀。
- 已带有对应前缀的值不会重复添加前缀。

上述规则必须与 Servlet WebMVC Resource Server 保持一致。

## 3. 配置示例

```yaml
synapse:
  security:
    resource-server:
      enabled: true
      issuer-uri: http://127.0.0.1:8100
      jwk-set-uri: http://127.0.0.1:8100/oauth2/jwks
      permit-paths:
        - /actuator/health
        - /error
      csrf-enabled: false
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.security.resource-server.enabled` | `true` | 是否启用 Reactive OAuth2 Resource Server 自动配置 |
| `synapse.security.resource-server.issuer-uri` | 无 | 预期 JWT issuer；未提供 `jwk-set-uri` 时也用于创建默认 `ReactiveJwtDecoder` |
| `synapse.security.resource-server.jwk-set-uri` | 无 | JWK Set 地址，用于远程加载 JWT 验签公钥 |
| `synapse.security.resource-server.permit-paths` | `/actuator/health`, `/error` | 无需认证即可访问的 WebFlux 路径 |
| `synapse.security.resource-server.csrf-enabled` | `false` | 是否启用 Spring Security CSRF 防护 |

发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，并为上述配置项提供 IDE 可读说明。

## 4. Reactor Context

WebFlux 场景通过 Reactor Context 读取：

```java
SynapseReactiveSecurityContext.currentPrincipal();
SynapseReactiveSecurityContext.currentUser();
SynapseReactiveSecurityContext.currentClient();
SynapseReactiveOperationContext.currentOperationContext();
```

不得依赖 Servlet ThreadLocal 作为唯一上下文。

## 5. 自动配置边界

默认自动配置会在 Reactive Web 环境中装配：

- JWT 到 Synapse principal 的 converter。
- Reactive SecurityContext / OperationContext 读取能力。
- 401/403 响应适配。
- 缺少用户自定义 `ReactiveJwtDecoder` 时，按 `jwk-set-uri` 或 `issuer-uri` 创建默认 decoder。
- 缺少用户自定义 `SecurityWebFilterChain` 时，创建默认 Resource Server filter chain。

用户自定义 `SecurityWebFilterChain` 后，默认链退让。复杂网关或平台鉴权应显式调用 `SynapseResourceServerServerHttpSecurityConfigurer`，但 Gateway 路由和网关业务鉴权不进入 Framework。

## 6. 边界

该模块不是 Gateway 服务，不提供 RouteLocator、Gateway Filter 业务逻辑、网关鉴权后台或启动服务，也不创建私钥、`RSAKey` 或 `JwtEncoder`。
