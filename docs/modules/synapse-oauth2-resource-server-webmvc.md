# synapse-oauth2-resource-server-webmvc 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webmvc` 提供 Servlet OAuth2 Resource Server 技术适配。

当前能力：

- `SynapseResourceServerProperties`
- `SpringJwtClaimAccessor`
- `SynapseJwtAuthenticationConverter`
- `SynapseJwtPrincipalMapper`
- `SynapseJwtGrantedAuthoritiesConverter`
- `SynapseSecurityContextBridgeFilter`
- 默认 `SecurityFilterChain`
- `SynapseResourceServerConfigurer`
- 统一 401/403 `Result` 写出

## 2. 主体映射

- `principal_type=USER` 映射为 `AuthenticatedUser`，用户标识来自 `sub`。
- `principal_type=CLIENT` 映射为 `AuthenticatedClient`，客户端标识来自 `client_id`。
- 主体类型使用 Core 的 `SynapsePrincipalType` 协议值判断，未知类型直接拒绝。
- CLIENT 不会被伪装成 USER。
- roles / permissions 只保存在 `SecurityContext`，不进入 `OperationContext`。

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

用户自定义 `SecurityFilterChain` 后，默认链退让。复杂 IAM 应显式调用 `SynapseResourceServerConfigurer`。

## 5. 边界

Resource Server 不创建私钥、`RSAKey` 或 `JwtEncoder`，也不实现登录、客户端管理或 IAM。
