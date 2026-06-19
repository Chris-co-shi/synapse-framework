# synapse-security Skill

## 职责

`synapse-security` 只提供 Web 无关安全主体、SecurityContext、权限检查抽象、权限注解适配和密码编码能力。

认证主体由 OAuth2 Resource Server 等专用适配模块在验证 Bearer Token 后建立；security 本身不读取 HTTP 请求或 Token。

## 认证边界

- Gateway 与下游服务之间只传播 Bearer Token。
- 下游服务必须独立验证签名、issuer、audience、有效期和 token contract。
- 不传播或信任用户、角色、权限等身份 Header。
- 不恢复身份 Header 认证协议，不提供 HMAC Header 签名和时间戳校验。

## 禁止事项

- 不做 IAM。
- 不做登录认证、用户注册、密码校验流程。
- 不做用户、角色、菜单、组织管理。
- 不继续新增 OAuth2 / JWT / JWK 能力；这些属于 OAuth2 拆分模块。
- 不新增 Servlet Filter / WebFilter 或 Web 认证入口。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 安全主体模型必须保持轻量。
- `AuthenticatedClient` 不得伪装为 `AuthenticatedUser`。
- 权限注解只调用 `PermissionChecker`，不内置业务权限数据来源。
- roles 和 permissions 不得进入 `OperationContext`。
- 业务代码只通过 `SecurityContext` 只读门面访问当前主体。

## 验证

- 运行 `mvn -q -pl synapse-security -am test`。
- 搜索身份 Header、IAM、登录、用户/角色/菜单等残留概念。
- 修改 `SynapseSecurityProperties` 时必须验证 `synapse.security.*` Spring Boot Configuration Metadata。
