# synapse-oauth2-core Skill

## 职责

提供 OAuth2/JWT 协议无关契约：claim 常量、token 类型、validator、denylist 端口和 `BearerTokenProvider`。

## 禁止事项

- 不依赖 Web / Security / Servlet / WebFlux。
- 不创建私钥、`JwtEncoder`、`JwtDecoder`。
- 不做 IAM、登录、授权后台。

## 测试要求

- validator 覆盖 required claim、audience、token_type、principal_type、denylist。
- token 不进入 OperationContext 或 MQ。
