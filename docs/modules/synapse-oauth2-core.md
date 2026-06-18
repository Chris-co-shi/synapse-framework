# synapse-oauth2-core 使用手册

## 1. 模块定位

`synapse-oauth2-core` 是 Web 和 Spring Security 无关的 OAuth2/JWT 基础契约模块。

当前能力：

- `SynapseJwtClaimNames`
- `SynapsePrincipalType`
- `SynapseTokenType`
- `JwtClaimAccessor`
- `JwtClaimValues`
- `TokenDenylistPort`
- `NoopTokenDenylistPort`
- `BearerTokenProvider`
- 协议无关 JWT validator
- `OAuth2ErrorCode`

## 2. JWT claim 统一契约

`SynapsePrincipalType` 定义 `principal_type` 的稳定协议值：

- `USER`：用户主体，Resource Server 从 `sub` 读取用户标识。
- `CLIENT`：客户端或服务主体，Resource Server 从 `client_id` 读取客户端标识。

`JwtClaimAccessor` 是不同 JWT 技术实现与 Core 之间的读取端口。WebMVC、WebFlux 等适配模块只负责暴露原始 claim，不在适配器内重复实现规范化规则。

`JwtClaimValues` 提供统一读取规则：

- `requiredString()` 对缺失或空白必填 claim 抛出 `IllegalArgumentException`。
- `strings()` 去除首尾空白、过滤空值、按首次出现顺序去重，并返回不可修改集合。
- roles、permissions、scope 等字符串集合 claim 必须通过该工具读取，避免不同 Resource Server 产生不同解释。

## 3. 边界

该模块不得依赖：

- `synapse-security`
- Servlet / WebFlux
- Spring Security Web / Config
- Authorization Server 签发支持

它不创建私钥、不创建 `JwtEncoder`、不创建 Resource Server。

## 4. 适用场景

- Resource Server 共享 JWT claim 契约。
- Cloud Feign 通过 `BearerTokenProvider` 读取当前 token。
- 业务或平台提供自己的 denylist 实现。

Bearer Token 不得进入 `OperationContext`、MQ Header 或日志。
