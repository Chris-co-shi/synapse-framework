# synapse-security Skill

## 职责

`synapse-security` 只提供轻量安全上下文、trusted-header、权限检查抽象和权限注解适配。

## 禁止事项

- 不做 IAM。
- 不做登录认证、用户注册、密码校验。
- 不做用户、角色、菜单、组织管理。
- 不继续新增 OAuth2 / JWT / JWK 能力；这些属于 `synapse-oauth2`。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 安全主体模型必须保持轻量。
- trusted-header 只表达可信请求头校验，不等同平台认证。
- 权限注解只调用 `PermissionChecker`，不内置业务权限数据来源。

## 验证

- 运行 `mvn -q -pl synapse-security -am test`。
- 搜索 IAM、登录、用户/角色/菜单等业务概念。
- 修改 `SynapseSecurityProperties` 时必须验证 `synapse.security.*` Spring Boot Configuration Metadata。
