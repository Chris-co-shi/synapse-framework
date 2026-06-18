# synapse-oauth2-core Skill

## 职责

提供 OAuth2/JWT 协议无关契约：claim 常量、主体类型、token 类型、validator、denylist 端口和 `BearerTokenProvider`。

## Claim 规范

- 主体类型统一使用 `SynapsePrincipalType`。
- 必填字符串统一通过 `JwtClaimValues.requiredString()` 读取。
- roles、permissions、scope 统一通过 `JwtClaimValues.strings()` 规范化。
- WebMVC 与 WebFlux 适配器只读取原始 claim，不复制 Core 规范化逻辑。

## 禁止事项

- 不依赖 Web / Security / Servlet / WebFlux。
- 不创建私钥、`JwtEncoder`、`JwtDecoder`。
- 不做 IAM、登录、授权后台。

## 测试要求

- validator 覆盖 required claim、audience、token_type、principal_type、denylist。
- claim 工具覆盖缺失和空白必填值、空白过滤、去重与顺序保持。
- token 不进入 OperationContext 或 MQ。
