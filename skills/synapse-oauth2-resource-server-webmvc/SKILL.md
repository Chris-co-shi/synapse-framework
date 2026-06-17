# synapse-oauth2-resource-server-webmvc Skill

## 职责

提供 Servlet OAuth2 Resource Server 技术适配、JWT 到 Synapse Principal 映射、Context Bridge 和统一 401/403。

## 禁止事项

- 不依赖 authorization-server-support。
- 不创建私钥、RSAKey、JwtEncoder。
- 不做 IAM、登录、客户端管理。
- 不直接依赖 data/audit/mq。

## 测试要求

- USER / CLIENT 映射。
- CLIENT 不伪装成 USER。
- 默认链在用户自定义 SecurityFilterChain 后退让。
- 401/403 复用 synapse-webmvc writer。
