# synapse-oauth2-authorization-server-support 使用手册

## 1. 模块定位

`synapse-oauth2-authorization-server-support` 只提供 JWT 签发技术支持。

当前能力：

- `SigningKeyPolicy`
- `SynapseRsaKeyFactory`
- `SigningKeyProvider`
- `SigningKeySetProvider`
- `JwtIssuanceClaims`
- `SynapseJwtIssuer`
- `SynapseAuthorizationServerSupportAutoConfiguration`

## 2. 不提供

- 登录。
- 用户密码校验。
- RegisteredClient 管理。
- Authorization Code。
- Refresh Token 存储。
- Consent。
- OIDC 页面。
- IAM / RBAC。

## 3. 开发密钥策略

默认不生成开发私钥。只有显式配置：

```yaml
synapse:
  oauth2:
    authorization:
      development-key-enabled: true
```

才会生成运行时开发 RSAKey。`production=true` 时禁止生成开发密钥。
