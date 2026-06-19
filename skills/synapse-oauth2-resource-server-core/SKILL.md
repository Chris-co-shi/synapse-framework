# synapse-oauth2-resource-server-core Skill

## 职责

统一 MVC/WebFlux 的 JWT 验证策略、USER/CLIENT 主体映射、authority 顺序和认证失败模型。

## 共享规则

- 两种适配器都必须从 `ResourceServerValidationPolicy` 创建 validator。
- 主体映射统一使用 `SynapsePrincipalClaimMapper`。
- authority 统一使用 `SynapseAuthorityClaimMapper`，顺序固定为 scope、roles、permissions。
- denylist 开启时必须提供真实 `TokenDenylistPort`，不得使用 Noop 静默放行。

## 禁止事项

- 不引用 Servlet、Spring MVC、Reactor 或 WebFlux 类型。
- 不创建 decoder、私钥、JwtEncoder 或 SecurityFilterChain。
- 不查询用户、角色、权限或客户端数据库。
