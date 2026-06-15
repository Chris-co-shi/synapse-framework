# synapse-oauth2 Skill

## 职责

`synapse-oauth2` 只提供 JWT / JWK / token denylist 等 OAuth2 技术辅助能力。

## 禁止事项

- 不做 IAM。
- 不做登录、注册、密码校验、用户认证。
- 不做客户端管理后台、授权后台。
- 不实现 OAuth2 Authorization Server。
- 不新增 Resource Server FilterChain。
- 不新增用户、角色、菜单、组织等业务模型。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- JWT 签发和校验通过 `SynapseJwtService`。
- claims 使用 `JwtClaims`，不得塞入角色、权限、菜单或组织结构。
- token 主动失效通过 `TokenDenylistPort` 扩展。
- 生产环境必须由消费方提供真实密钥和 denylist 实现。

## 测试要求

- 覆盖 JWT 签发和校验。
- 覆盖非法 token。
- 覆盖 denylist。
- 覆盖生产环境保护策略。
- 覆盖自定义 Bean 不覆盖。

## 必读

- `AGENTS.md`
- `docs/modules/synapse-oauth2.md`
- `docs/phase-2/00-framework-boundary.md`
