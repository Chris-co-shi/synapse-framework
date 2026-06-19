# synapse-oauth2-client Skill

## 职责

提供协议中立的 Token Relay、Client Credentials、Authorized Client token 存储端口和 token 生命周期编排。

## 使用规则

- 入站 token relay 使用 `TokenRelayProvider`，只读取已验证 token，不解析 claim。
- Client Credentials 使用 `OAuth2ClientTokenManager`，通过固定 `Clock` 测试提前刷新。
- 生产集群应实现 `AuthorizedClientTokenStore`；内存实现不保证持久化或集群一致性。
- 具体 HTTP 客户端只依赖 `OutboundBearerTokenProvider`，不得记录 token 原值。

## 禁止事项

- 不包装 `@FeignClient` 或创建通用 HTTP 客户端。
- 不把 client token 写入 `CurrentPrincipalContext`、OperationContext、MQ 或审计属性。
- 不提供 client secret 管理、登录、IAM 或客户端管理后台。
