# synapse-oauth2-resource-server-webmvc 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webmvc` 提供 Servlet OAuth2 Resource Server 技术适配。

当前能力：

- `SynapseResourceServerProperties`
- `SpringJwtClaimAccessor`
- `SynapseJwtAuthenticationConverter`
- `SynapseJwtPrincipalMapper`
- `SynapseJwtGrantedAuthoritiesConverter`
- `SynapsePrincipalContextBridgeFilter`
- `GatewayProofVerificationFilter`
- 默认 `SecurityFilterChain`
- `SynapseResourceServerConfigurer`
- 统一 401/403 `Result` 写出

## 2. 主体映射

- `principal_type=USER` 映射为 `AuthenticatedUser`，用户标识来自 `sub`。
- `principal_type=CLIENT` 映射为 `AuthenticatedClient`，客户端标识来自 `client_id`。
- 主体类型使用 Core 的 `SynapsePrincipalType` 协议值判断，未知类型直接拒绝。
- CLIENT 不会被伪装成 USER。
- roles / permissions 只保存在 `CurrentPrincipalContext`，不进入 `OperationContext`。

`SynapsePrincipalContextBridgeFilter` 位于 Bearer Token 认证过滤器之后，只接受
`SynapseJwtAuthenticationToken` 中已经映射完成的主体。它通过
`PrincipalContextScope` 绑定当前请求，并在过滤器链正常返回或抛出异常时自动清理，
防止 Servlet 容器线程复用导致主体串线。

## 3. Claim 与 Authority 规则

`SpringJwtClaimAccessor` 只把 Spring Security `Jwt` 适配成 Core 的 `JwtClaimAccessor`。必填校验、空白过滤、去重和顺序保持统一由 `JwtClaimValues` 完成。

`SynapseJwtGrantedAuthoritiesConverter` 按固定顺序生成 authority：

1. scope：增加 `SCOPE_` 前缀。
2. roles：增加 `ROLE_` 前缀。
3. permissions：增加 `PERM_` 前缀。

已经带有对应前缀的值不会被重复添加前缀。空白值被过滤，同一原始值按首次出现顺序去重。

## 4. 配置示例

```yaml
synapse:
  security:
    resource-server:
      enabled: true
      issuer-uri: http://127.0.0.1:8100
      jwk-set-uri: http://127.0.0.1:8100/oauth2/jwks
      audiences:
        - message-service
      denylist-enabled: false
```

用户自定义 `SecurityFilterChain` 后，默认链退让。复杂应用应显式调用 `SynapseResourceServerConfigurer`。

启用 GatewayProof 时，`GatewayProofVerificationFilter` 会插入到 `BearerTokenAuthenticationFilter` 之前。它只校验可信 Gateway proof，不建立认证主体；JWT 仍由后续 Resource Server 过滤器独立验证。

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.security.resource-server.enabled` | `true` | 是否启用 Servlet OAuth2 Resource Server 自动配置 |
| `synapse.security.resource-server.issuer-uri` | 无 | 预期 JWT issuer，用于 issuer claim 校验和默认 JwtDecoder 配置 |
| `synapse.security.resource-server.jwk-set-uri` | 无 | JWK Set 地址，用于远程加载 JWT 验签公钥 |
| `synapse.security.resource-server.public-key-location` | 无 | 本地公钥资源位置；与 `jwk-set-uri` 互斥 |
| `synapse.security.resource-server.issuer-validation-enabled` | `true` | 是否校验 JWT issuer claim |
| `synapse.security.resource-server.audience-validation-enabled` | `true` | 是否校验 JWT audience claim |
| `synapse.security.resource-server.audiences` | 空列表 | 当前服务接受的 JWT audience 列表 |
| `synapse.security.resource-server.accepted-token-types` | `[ACCESS_TOKEN]` | 当前 Resource Server 接受的 Synapse `token_type` 协议值 |
| `synapse.security.resource-server.required-claims` | `sub`, `exp`, `iat`, `token_type`, `principal_type` | JWT 中必须存在的 claim 名称列表 |
| `synapse.security.resource-server.clock-skew` | `60s` | JWT 时间类 claim 校验允许的时钟偏移 |
| `synapse.security.resource-server.denylist-enabled` | `true` | 是否启用 token denylist 校验 |
| `synapse.security.resource-server.permit-paths` | `/actuator/health`, `/error` | 无需认证即可访问的 Servlet 路径 |
| `synapse.security.resource-server.csrf-enabled` | `false` | 是否启用 Spring Security CSRF 防护 |
| `synapse.security.resource-server.fail-fast` | `true` | 是否在配置不完整时启动失败 |

发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，并为上述配置项提供 IDE 可读说明；`accepted-token-types` 当前只暴露 `ACCESS_TOKEN` 候选值。

GatewayProof 复用 `synapse.security.gateway-proof.*` 配置，详见 [synapse-security](synapse-security.md) 和 [GatewayProof 可信入口证明](../phase-2/05-gateway-proof.md)。

## 5. 边界

Resource Server 不创建私钥、`RSAKey` 或 `JwtEncoder`，也不实现登录、客户端管理或 IAM。
GatewayProof Filter 不做 Gateway 服务、路由、网关鉴权业务或身份 Header 恢复。
