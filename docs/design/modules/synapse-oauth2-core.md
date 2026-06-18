# synapse-oauth2-core 设计说明

## 1. 模块使命

`synapse-oauth2-core` 定义 OAuth2/JWT 相关的稳定、协议级契约，使 Authorization Server、Servlet Resource Server、Reactive Resource Server 和 Cloud 能共享 claim、token type、validator、denylist 与 Bearer Token 读取抽象。

## 2. 为什么独立于 security

OAuth2/JWT 是传输与认证协议；`synapse-security` 是 Framework 内部主体模型。core OAuth2 不应依赖 AuthenticatedUser，也不应知道 Servlet、WebFlux 或 Spring Security FilterChain。

```text
OAuth2 core contract
  -> web-specific resource server adapter
  -> Synapse security principal
```

## 3. 边界

负责：

- Synapse JWT claim 名称。
- token type 枚举/约定。
- 协议无关 validator。
- `TokenDenylistPort`。
- `BearerTokenProvider`。
- OAuth2 技术错误码。

不负责：

- 创建 RSA 私钥、JWKSource、JwtEncoder。
- Spring Security Authentication。
- HTTP Bearer Filter。
- Synapse SecurityContext。
- 登录、授权码、Refresh Token 和客户端管理。

## 4. 核心对象角色

### 4.1 `SynapseJwtClaimNames`

定义签发端与验证端共享的稳定 claim 协议。claim 名称发布后应保持兼容，不能因内部字段重命名随意变化。

### 4.2 `SynapseTokenType`

区分 access token 等技术类型，使 validator 不只依赖“它是合法 JWT”，还验证是否适用于当前入口。

### 4.3 `TokenDenylistPort`

抽象 token 撤销检查。Framework 提供 Noop 只是可选能力的空实现，不代表生产环境已经支持撤销。

### 4.4 `BearerTokenProvider`

为 Cloud 等非 Web 核心模块提供读取当前 token 的端口，而不让它们依赖 Spring Security。Token 仍不得进入 OperationContext、MQ Header 或日志。

### 4.5 validators

对 issuer、audience、token type、denylist 等协议事实进行验证。validator 不查询用户权限，也不负责主体映射。

## 5. 主链路

签发侧：

```text
Platform IAM claims
  -> shared claim names and token type
  -> authorization-server-support issuer
```

验证侧：

```text
decoded JWT
  -> protocol validators
  -> web adapter principal mapper
  -> synapse-security principal
```

## 6. 安全边界

- Noop denylist 不是生产级撤销能力。
- Bearer Token 是凭证，不是普通上下文属性。
- claim 中的 roles/permissions 是签发快照，Resource Server 不应无条件信任未知 issuer。
- validator 顺序不能绕过签名、issuer、audience 与时间有效性。
- 错误响应不要返回 token 内容或详细密码学内部信息。

## 7. 扩展原则

- Redis / DB 撤销：消费方实现 `TokenDenylistPort`。
- 当前 token 读取：Web adapter 实现 `BearerTokenProvider`。
- 新 claim：先判断是否是跨签发/验证端稳定协议；业务临时字段不要进入 core。
- 新 validator 保持 Web 无关。

## 8. 源码阅读顺序

```text
SynapseJwtClaimNames
  -> SynapseTokenType
  -> OAuth2ErrorCode
  -> validator interfaces and implementations
  -> TokenDenylistPort / Noop implementation
  -> BearerTokenProvider
  -> tests
```

## 9. 手写练习

1. 定义 issuer、audience、token_type 验证器。
2. 用 fake denylist 拒绝指定 jti。
3. 验证 Noop denylist 永远不拒绝，但明确它不是生产保护。
4. 检查日志与 OperationContext 中都没有 raw token。

## 10. 修改检查清单

- 是否引入 Servlet、WebFlux 或 Spring Security Config。
- 是否依赖 `synapse-security`。
- 是否开始创建私钥或 JwtEncoder。
- 是否把业务字段变成公共 claim 协议。
- 是否让 raw token 进入日志、MQ 或 OperationContext。
- 默认空实现是否被错误描述为生产能力。
