# synapse-oauth2-client 使用手册

## 模块定位

`synapse-oauth2-client` 承载 OAuth2 出站 Token Relay、Client Credentials、Authorized
Client 管理和 Token 生命周期扩展。

## 当前能力

- `TokenRelayProvider` 通过 `BearerTokenProvider` 读取当前入站 token，不解析 claim。
- `ClientCredentialsTokenProvider` 定义 client credentials 获取端口。
- `AuthorizedClientTokenStore` 定义 authorized-client token 存储端口。
- `InMemoryAuthorizedClientTokenStore` 提供单实例和测试可用的线程安全内存实现。
- `OAuth2ClientTokenManager` 负责缓存、提前刷新和显式失效。
- `OutboundBearerTokenProvider` 是具体 HTTP 客户端写入 Authorization header 的扩展点。

`OAuth2ClientToken.value` 是敏感凭证，不得进入日志、审计 attributes 或
`CurrentPrincipalContext`。Framework 不提供 client secret 存储、数据库 token store 或 HTTP
token endpoint 客户端；这些由消费方适配 `ClientCredentialsTokenProvider`。

## 边界

- 不包装 `@FeignClient`，不替代 Spring Cloud OpenFeign。
- 不把出站 Client Token 写入当前主体上下文。
- 不实现登录、客户端管理后台或 IAM 服务。
- 不提供全局默认 client token，调用方必须明确 registrationId。
- 内存 token store 不具备持久化和集群一致性，生产集群应提供自定义 store。
