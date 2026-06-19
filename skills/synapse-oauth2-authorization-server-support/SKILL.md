# synapse-oauth2-authorization-server-support Skill

## 职责

只提供 JWT 签发支持、RSAKey/JWKSource/JwtEncoder 和 `SynapseJwtIssuer`。

## 配置

- 配置前缀：`synapse.oauth2.authorization`。
- 公开配置项必须生成 Spring Boot Configuration Metadata。
- `key-id` 必须通过 additional metadata 标注 `synapse-dev` 仅适合开发环境。
- `production=true` 时禁止自动生成开发 RSAKey。
- `key-id` 生产环境不得使用默认开发值。

## 禁止事项

- 不实现登录。
- 不实现 RegisteredClient、Authorization Code、Refresh Token、Consent、OIDC 页面。
- 不做 IAM / RBAC。
- 生产环境不自动生成开发私钥。

## 测试要求

- 开发密钥默认关闭。
- production 禁止生成开发密钥。
- 签发 token 包含 token_type 和 principal_type。
- metadata 测试应覆盖公开配置项。
