# synapse-oauth2-resource-server-webflux Skill

## 职责

提供 Reactive OAuth2 Resource Server 技术适配、Reactor Context 安全/操作上下文读取和统一 401/403。

## Claim 规则

- `SpringJwtClaimAccessor` 只适配原始 Spring Security JWT claim。
- 必填校验与集合规范化复用 Core 的 `JwtClaimValues`。
- authority 固定按 scope、roles、permissions 顺序生成。
- 对应前缀依次为 `SCOPE_`、`ROLE_`、`PERM_`，已有前缀不得重复添加。
- 主体和 authority 语义必须与 WebMVC Resource Server 保持一致。

## 禁止事项

- 不做 Gateway 服务。
- 不依赖 Servlet ThreadLocal。
- 不创建私钥、RSAKey、JwtEncoder。
- 不实现 IAM、登录、客户端管理。

## 测试要求

- Reactive USER / CLIENT 映射。
- 未知主体类型与缺失必填 claim 必须拒绝。
- roles、permissions、scope 覆盖空白过滤、去重、前缀和顺序。
- Reactor Context 读取 principal 和 OperationContext。
- 401/403 复用 synapse-webflux writer。
