# synapse-security Skill

## 职责

`synapse-security` 只提供轻量安全上下文、trusted-header、权限检查抽象和权限注解适配。

## 禁止事项

- 不做 IAM。
- 不做登录认证、用户注册、密码校验。
- 不做用户、角色、菜单、组织管理。
- 不继续新增 OAuth2 / JWT / JWK 能力；这些属于 `synapse-oauth2`。
- 不实现 nonce 持久化、ABAC 或 DataScope。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

### 安全主体

- 安全主体模型必须保持轻量。
- roles 和 permissions 只作为当前请求的安全快照。
- `OperationContext` 不承载 roles、permissions、菜单或业务权限规则。
- security 只向 core `OperationContext` 单向适配，不允许 core、data、audit 反向依赖 security。

### 上下文生命周期

- 同步入口存在嵌套可能时，必须使用 `SecurityContext.scope(...)` 和 try-with-resources。
- `SecurityContext.set(...)` 表示替换当前用户，调用方必须保证最终执行 `clear()`。
- `SecurityContext.scope(...)` 表示嵌套作用域，关闭时必须精确恢复外层用户和 OperationContext。
- `SecurityContext.scope(null)` 表示临时未认证作用域，不能继承外层认证用户。
- 重复 `clear()` 和重复 scope close 必须安全。
- 没有 SecurityContext 绑定时，不得清除独立存在的 Job、Async 或 MQ OperationContext。
- ThreadLocal 作用域必须在创建它的同一线程关闭。

### trusted-header

- trusted-header 只表达可信请求头校验，不等同平台认证。
- trusted-header 认证阶段与下游 FilterChain 执行阶段必须分离。
- 下游抛出的认证异常不能被 trusted-header Filter 当作 Header 认证失败处理。
- 每个请求的 FilterChain 最多执行一次。
- `fail-fast=false` 时，下游必须在未认证作用域执行，不能沿用外层 SecurityContext。
- Filter 结束后必须恢复外层 SecurityContext 和 OperationContext，不能无条件清空外层上下文。
- timestamp tolerance 必须覆盖过去、未来、精确边界和零容忍窗口。

### 权限检查

- 权限注解只调用 `PermissionChecker`，不内置业务权限数据来源。
- 显式 `PermissionChecker.require(...)` 与 `@RequirePermission` 必须返回一致的未认证和无权限错误码。
- 注解 AOP 不是 MQ、Task、Async 或内部调用的唯一安全边界；这些场景应显式检查权限。

## 验证

- 运行 `mvn -B -q -pl synapse-security -am test`。
- 运行 `mvn -B -q clean test`。
- 验证嵌套 scope、异常恢复、重复 clear、线程复用和跨线程关闭保护。
- 验证 trusted-header fail-fast / fail-open 和 FilterChain 单次执行。
- 验证显式权限入口与注解入口错误码一致。
- 搜索 IAM、登录、用户/角色/菜单等业务概念以及越界依赖。
