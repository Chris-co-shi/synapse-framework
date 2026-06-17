# synapse-oauth2 Legacy Skill

## 状态

`synapse-oauth2` 已拆分，不再是正式 reactor module。后续任务不得继续把它作为当前可实现模块。

## 替代模块

- `synapse-oauth2-core`：JWT claim、token、validator、denylist、BearerTokenProvider 契约。
- `synapse-oauth2-authorization-server-support`：JWT 签发、RSAKey、JWKSource、JwtEncoder 技术支撑。
- `synapse-oauth2-resource-server-webmvc`：Servlet Resource Server 技术适配。
- `synapse-oauth2-resource-server-webflux`：Reactive Resource Server 技术适配。

## 禁止事项

- 不恢复 `synapse-oauth2` 聚合模块。
- 不创建兼容 starter 或 demo。
- 不在 Framework 中实现 IAM、登录、客户端管理后台、授权后台或完整 Authorization Server 业务流程。

## 后续执行

遇到旧文档或旧引用时，应迁移到上述拆分模块，并同步更新 `README.md`、`AGENTS.md`、`docs/modules/README.md` 和对应模块 Skill。
