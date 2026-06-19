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

## 2. 签发能力

`SynapseJwtIssuer` 基于 Spring Security `JwtEncoder` 写出 Synapse 稳定 claim：

- `iss`
- `sub`
- `aud`
- `jti`
- `iat`
- `exp`
- `token_type`
- `principal_type`
- 调用方传入的 additional claims

`JwtIssuanceClaims` 会校验 issuer、subject、tokenId、tokenType、principalType、issuedAt、expiresAt 等必填值，并要求 `expiresAt` 晚于 `issuedAt`。

## 3. 配置项

前缀：

```yaml
synapse:
  oauth2:
    authorization:
      production: false
      key-id: synapse-dev
      development-key-enabled: false
```

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `synapse.oauth2.authorization.production` | `false` | 是否为生产环境；生产环境禁止自动生成开发签名密钥 |
| `synapse.oauth2.authorization.key-id` | `synapse-dev` | JWK key id；生产环境不得使用默认 key id；metadata 会提示该值仅适合开发环境 |
| `synapse.oauth2.authorization.development-key-enabled` | `false` | 是否启用开发 RSAKey 自动生成 |

发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`；`key-id` 通过 additional metadata 提供 `synapse-dev` 开发值提示。

## 4. 开发密钥策略

默认不生成开发私钥。只有显式配置：

```yaml
synapse:
  oauth2:
    authorization:
      development-key-enabled: true
```

才会生成运行时开发 RSAKey。`production=true` 时禁止生成开发密钥。

生产环境应由平台或业务系统显式提供 `RSAKey` / `SigningKeyProvider` / `SigningKeySetProvider` 等 Bean。

## 5. 不提供

- 登录。
- 用户密码校验。
- RegisteredClient 管理。
- Authorization Code。
- Refresh Token 存储。
- Consent。
- OIDC 页面。
- IAM / RBAC。
