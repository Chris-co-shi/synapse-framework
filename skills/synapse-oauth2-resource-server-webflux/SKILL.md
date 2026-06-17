# synapse-oauth2-resource-server-webflux Skill

## 职责

提供 Reactive OAuth2 Resource Server 技术适配、Reactor Context 安全/操作上下文读取和统一 401/403。

## 禁止事项

- 不做 Gateway 服务。
- 不依赖 Servlet ThreadLocal。
- 不创建私钥、RSAKey、JwtEncoder。
- 不实现 IAM、登录、客户端管理。

## 测试要求

- Reactive USER / CLIENT 映射。
- Reactor Context 读取 principal 和 OperationContext。
- 401/403 复用 synapse-webflux writer。
