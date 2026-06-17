# synapse-oauth2-core 使用手册

## 1. 模块定位

`synapse-oauth2-core` 是 Web 和 Spring Security 无关的 OAuth2/JWT 基础契约模块。

当前能力：

- `SynapseJwtClaimNames`
- `SynapseTokenType`
- `TokenDenylistPort`
- `NoopTokenDenylistPort`
- `BearerTokenProvider`
- 协议无关 JWT validator
- `OAuth2ErrorCode`

## 2. 边界

该模块不得依赖：

- `synapse-security`
- Servlet / WebFlux
- Spring Security Web / Config
- Authorization Server 签发支持

它不创建私钥、不创建 `JwtEncoder`、不创建 Resource Server。

## 3. 适用场景

- Resource Server 共享 JWT claim 契约。
- Cloud Feign 通过 `BearerTokenProvider` 读取当前 token。
- 业务或平台提供自己的 denylist 实现。

Bearer Token 不得进入 `OperationContext`、MQ Header 或日志。
