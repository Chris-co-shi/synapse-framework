# synapse-oauth2-resource-server-webmvc 使用手册

## 1. 模块定位

`synapse-oauth2-resource-server-webmvc` 提供 Servlet OAuth2 Resource Server 技术适配。

当前能力：

- `SynapseResourceServerProperties`
- `SynapseJwtAuthenticationConverter`
- `SynapseJwtPrincipalMapper`
- `SynapseSecurityContextBridgeFilter`
- 默认 `SecurityFilterChain`
- `SynapseResourceServerConfigurer`
- 统一 401/403 `Result` 写出

## 2. 主体映射

- `principal_type=USER` 映射为 `AuthenticatedUser`。
- `principal_type=CLIENT` 映射为 `AuthenticatedClient`。
- CLIENT 不会被伪装成 USER。
- roles / permissions 只保存在 `SecurityContext`，不进入 `OperationContext`。

## 3. 配置示例

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

## 4. 边界

Resource Server 不创建私钥、`RSAKey` 或 `JwtEncoder`，也不实现登录、客户端管理或 IAM。
