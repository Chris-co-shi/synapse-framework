# synapse-oauth2-resource-server-webmvc Skill

## 职责

提供 Servlet OAuth2 Resource Server 技术适配、JWT 到 Synapse Principal 映射、Context Bridge 和统一 401/403。

## Claim 规则

- `SpringJwtClaimAccessor` 只适配原始 Spring Security JWT claim。
- 必填校验与集合规范化复用 Core 的 `JwtClaimValues`。
- authority 固定按 scope、roles、permissions 顺序生成。
- 对应前缀依次为 `SCOPE_`、`ROLE_`、`PERM_`，已有前缀不得重复添加。

## 配置

- 配置前缀：`synapse.security.resource-server`。
- WebMVC 当前支持 issuer、JWK Set、本地公钥、issuer/audience 校验开关、audiences、accepted token types、required claims、clock skew、denylist、permit paths、CSRF、fail fast。
- 公开配置项必须生成 Spring Boot Configuration Metadata。
- `accepted-token-types` 当前只应暴露 `ACCESS_TOKEN` 候选值。

## 禁止事项

- 不依赖 authorization-server-support。
- 不创建私钥、RSAKey、JwtEncoder。
- 不做 IAM、登录、客户端管理。
- 不直接依赖 data/audit/mq。

## 测试要求

- USER / CLIENT 映射。
- 未知主体类型与缺失必填 claim 必须拒绝。
- roles、permissions、scope 覆盖空白过滤、去重、前缀和顺序。
- CLIENT 不伪装成 USER。
- 默认链在用户自定义 SecurityFilterChain 后退让。
- 401/403 复用 synapse-webmvc writer。
- metadata 测试应覆盖配置项说明和必要 hints。
