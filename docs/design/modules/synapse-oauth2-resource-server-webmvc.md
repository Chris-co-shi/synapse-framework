# synapse-oauth2-resource-server-webmvc 设计说明

## 1. 模块使命

该模块把 Spring Security Servlet OAuth2 Resource Server 认证结果适配为 Synapse 的 `AuthenticatedPrincipal`、SecurityContext 和 OperationContext，并提供统一 401/403 响应。

## 2. 边界

负责：

- Resource Server properties 与 validators 组装。
- JWT claims 到 Spring Authentication 的转换。
- JWT claims 到 USER / CLIENT 主体的映射。
- Spring SecurityContext 到 Synapse SecurityContext 的 Bridge Filter。
- GatewayProof 前置校验 Filter。
- 401 entry point 与 403 access denied handler。
- 默认 `SecurityFilterChain` 或可复用 configurer。

不负责：

- JWT 私钥和签发。
- 登录、客户端管理、Refresh Token。
- IAM 数据库和权限后台。
- 业务 URL 权限规则全集。

## 3. 两套上下文的关系

```text
Spring SecurityContext
  -> Authentication framework integration

Synapse SecurityContext
  -> stable framework principal
  -> PermissionChecker
  -> OperationContext adapter
```

Bridge 不是重复认证，而是把框架外部认证对象转换为内部稳定模型。

## 4. 核心对象角色

- `SynapseJwtPrincipalMapper`：claims -> USER / CLIENT。
- `SynapseJwtGrantedAuthoritiesConverter`：claims -> Spring authorities。
- `SynapseJwtAuthenticationConverter`：组装 `SynapseJwtAuthenticationToken`。
- `SynapseSecurityContextBridgeFilter`：认证完成后打开 Synapse scope。
- `SynapseResourceServerConfigurer`：无状态、访问策略、异常 handler、converter 和 Filter 顺序组装。
- AutoConfiguration：验证配置并提供默认链，用户自定义链时退让。

## 5. 主链路

```text
Authorization: Bearer token + GatewayProof Headers
  -> GatewayProofVerificationFilter
  -> BearerTokenAuthenticationFilter
  -> JwtDecoder + signature/time/issuer/audience validators
  -> SynapseJwtAuthenticationConverter
  -> SynapseJwtPrincipalMapper
  -> SynapseJwtAuthenticationToken
  -> Spring SecurityContextHolder
  -> SynapseSecurityContextBridgeFilter
  -> SecurityContextBinder.bind
  -> OperationContext
  -> Controller / Service / PermissionChecker
  -> scope.close
```

## 6. 主体映射规则

```text
principal_type=USER
  sub -> userId
  preferred_username -> username, blank fallback to userId

principal_type=CLIENT
  client_id -> clientId
```

`principal_type` 必须明确。未知类型不能默认当成 USER。

roles / permissions 是 token 快照；mapper 不查询数据库，也不根据 role 推导权限。

## 7. 401 与 403

- 401：没有成功建立 Authentication，例如 token 缺失、过期、签名/issuer/audience/denylist 校验失败。
- 403：已经认证，但 URL 或方法权限判断拒绝。

错误响应应统一 Result，但不得暴露 token、签名细节或内部验证堆栈。

## 8. Filter 顺序与生命周期

GatewayProof Filter 必须位于 Bearer Token Filter 之前，只校验可信入口证明，不建立认证主体。

Bridge Filter 必须位于 Bearer Token Filter 之后，否则读取不到已认证的 Synapse token。Bridge 使用 try-with-resources，确保 Controller 或后续 Filter 抛异常时仍恢复旧 SecurityContext / OperationContext。

## 9. 扩展原则

- 自定义 URL 规则：消费方声明 SecurityFilterChain 并复用 configurer。
- 自定义 denylist：实现 OAuth2 core Port。
- 自定义主体 claims：替换 mapper/converter Bean，同时维持 USER/CLIENT 语义。
- 业务方法权限继续通过 PermissionChecker 或业务授权层，不把全部规则硬编码进 Framework 默认链。

## 10. 源码阅读顺序

```text
SynapseResourceServerProperties
  -> validators / JwtDecoder assembly
  -> SynapseJwtPrincipalMapper
  -> GrantedAuthoritiesConverter
  -> SynapseJwtAuthenticationToken
  -> SynapseJwtAuthenticationConverter
  -> SynapseSecurityContextBridgeFilter
  -> 401 / 403 handlers
  -> SynapseResourceServerConfigurer
  -> AutoConfiguration
  -> integration tests
```

## 11. 手写练习

1. 构造 USER JWT claims 并转换为 Authentication。
2. 构造 CLIENT claims，验证不会成为 currentUser。
3. 缺失 audience 或 principal_type，验证认证失败。
4. Controller 抛异常后验证 Synapse SecurityContext 已清理。

## 12. 修改检查清单

- 是否创建了私钥或 JwtEncoder。
- 是否把 CLIENT 当 USER。
- 是否绕过 issuer/audience/time 校验。
- 是否把 token 内容写入错误响应或日志。
- Bridge Filter 是否位于正确顺序并关闭 scope。
- 默认 SecurityFilterChain 是否抢占用户自定义链。
- 401 和 403 是否被错误合并。
