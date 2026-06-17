# synapse-oauth2-authorization-server-support Skill

## 职责

只提供 JWT 签发支持、RSAKey/JWKSource/JwtEncoder 和 `SynapseJwtIssuer`。

## 禁止事项

- 不实现登录。
- 不实现 RegisteredClient、Authorization Code、Refresh Token、Consent、OIDC 页面。
- 不做 IAM / RBAC。
- 生产环境不自动生成开发私钥。

## 测试要求

- 开发密钥默认关闭。
- production 禁止生成开发密钥。
- 签发 token 包含 token_type 和 principal_type。
