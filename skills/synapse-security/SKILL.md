# synapse-security Skill

## 职责

`synapse-security` 只提供 Web 无关安全主体、CurrentPrincipalContext、权限检查抽象、权限注解适配、密码编码能力和 GatewayProof 协议基础。

认证主体由 OAuth2 Resource Server 等专用适配模块在验证 Bearer Token 后建立；security 本身不读取 HTTP 请求或 Token。

## 认证边界

- Gateway 与下游服务之间只传播 Bearer Token。
- 下游服务必须独立验证签名、issuer、audience、有效期和 token contract。
- 不传播或信任用户、角色、权限等身份 Header。
- 不恢复身份 Header 认证协议。
- GatewayProof 只证明可信入口，不替代 JWT 验证，不写 Servlet Filter / WebFilter。

## 禁止事项

- 不做 IAM。
- 不做登录认证、用户注册、密码校验流程。
- 不做用户、角色、菜单、组织管理。
- 不继续新增 OAuth2 / JWT / JWK 能力；这些属于 OAuth2 拆分模块。
- 不新增 Servlet Filter / WebFilter 或 Web 认证入口。
- 不实现 Gateway 服务、RouteLocator、GlobalFilter 或网关业务鉴权。
- 不新增 Controller、Entity、Mapper、Repository、migration。
- 不创建 starter、demo、example、sample application。

## 标准实现

- 安全主体模型必须保持轻量。
- `AuthenticatedClient` 不得伪装为 `AuthenticatedUser`。
- 权限注解只调用 `PermissionChecker`，不内置业务权限数据来源。
- roles 和 permissions 不得进入 `OperationContext`。
- 业务代码只通过 `CurrentPrincipalContext` 只读门面访问当前主体。
- Servlet ThreadLocal 只能存在于可关闭 Scope 中，正常和异常路径都必须恢复外层状态。
- Reactive 链路必须使用 Reactor Context，不得回退读取 ThreadLocal。
- GatewayProof canonical string、HMAC-SHA256 签名、常量时间比较、nonce replay store SPI 必须保持 Web 无关。
- 启用 replay protection 时不得提供 noop store；缺少 store 必须 fail fast 或请求期返回配置错误。

## 验证

- 运行 `mvn -q -pl synapse-security -am test`。
- 验证嵌套 Scope、异常清理、线程池复用和并发线程隔离。
- WebFlux 适配变更需验证 `publishOn`、`subscribeOn` 和并发订阅隔离。
- 搜索身份 Header、IAM、登录、用户/角色/菜单等残留概念。
- 修改 `SynapseSecurityProperties` 时必须验证 `synapse.security.*` Spring Boot Configuration Metadata。
- GatewayProof 变更必须覆盖 canonical query、签名失败、过期、重放和配置非法测试。
