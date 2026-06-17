# synapse-oauth2 历史说明

`synapse-oauth2` 已在安全与 OAuth2 架构重构中拆分，不再是正式 reactor module，也不再作为当前可引入能力描述。

## 当前替代模块

| 需求 | 当前模块 |
| --- | --- |
| JWT claim、token 类型、validator、denylist 端口、BearerTokenProvider | `synapse-oauth2-core` |
| JWT 签发、RSAKey、JWKSource、JwtEncoder 技术支撑 | `synapse-oauth2-authorization-server-support` |
| Servlet OAuth2 Resource Server 技术适配 | `synapse-oauth2-resource-server-webmvc` |
| Reactive OAuth2 Resource Server 技术适配 | `synapse-oauth2-resource-server-webflux` |

## 边界结论

- 不再新增或维护 `synapse-oauth2` 作为聚合模块。
- 不通过 `synapse-oauth2` 兼容层间接引入拆分后的模块。
- 不在 Framework 中实现 IAM、登录、用户认证、客户端管理后台或完整 Authorization Server 业务流程。
- 业务系统或 Platform 服务应按需直接引用拆分后的具体 module。

## 迁移建议

- 原本只需要 token / claim / validator 契约的场景，改用 `synapse-oauth2-core`。
- 原本需要签发 JWT 的 IAM 服务，改用 `synapse-oauth2-authorization-server-support`，并在 Platform 自己实现登录、客户端、授权流程和持久化。
- 普通 Servlet MVC 业务服务接入 Bearer Token 校验，改用 `synapse-oauth2-resource-server-webmvc`。
- Gateway 或 Reactive 服务接入 Bearer Token 校验，改用 `synapse-oauth2-resource-server-webflux`。
