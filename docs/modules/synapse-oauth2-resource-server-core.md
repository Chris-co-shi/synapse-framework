# synapse-oauth2-resource-server-core 使用手册

## 模块定位

`synapse-oauth2-resource-server-core` 用于统一 MVC 与 WebFlux Resource Server 的验证语义。

## 当前能力

- `ResourceServerValidationPolicy` 统一 issuer、audience、required claims、token type、clock skew 和 denylist 开关。
- `ResourceServerValidatorFactory` 以固定顺序组合协议 validator，并在 denylist 缺少真实端口时快速失败。
- `SynapsePrincipalClaimMapper` 统一 USER/CLIENT 主体映射。
- `SynapseAuthorityClaimMapper` 按 scope、roles、permissions 顺序生成 authority。
- `ResourceServerAuthenticationFailure` 提供不含 token 和 claim 原值的共享失败模型。

MVC 与 WebFlux 都通过各自的 `SpringJwtClaimAccessor` 适配 Spring Security `Jwt`，共享模块
不依赖 Spring Security Web。相同 claim 输入在两种技术栈中得到相同主体与 authority 结果。

## 边界

- 不得包含 Servlet、Spring MVC、Reactor 或 WebFlux 类型。
- 不提供 JWT 签发、私钥、登录或 Authorization Server 能力。
