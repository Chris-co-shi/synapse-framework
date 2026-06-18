# synapse-security 设计说明

## 1. 模块使命

`synapse-security` 定义 Web 无关的认证主体、安全上下文和权限检查契约，使业务 Service、任务、MQ 和不同 Web 技术栈可以共享同一安全模型，而不直接依赖 Spring Security Web。

## 2. 边界

负责：

- `AuthenticatedPrincipal`、USER 与 CLIENT 主体模型。
- ThreadLocal `SecurityContext` 与可关闭 Scope。
- 安全主体到 core `OperationContext` 的单向适配。
- `PermissionChecker` 与 `@RequirePermission` 轻量适配。
- trusted-header 的纯协议对象、签名和时间戳验证。
- 密码编码器工厂。

不负责：

- 登录、用户表、角色菜单后台。
- JWT/JWK 解析和 OAuth2 Resource Server。
- Servlet Filter / WebFilter。
- Spring Security FilterChain。
- ABAC、DataScope 和多租户授权规则。

## 3. 两类主体必须分开

```text
AuthenticatedUser
  -> human identity
  -> OperationActorType.USER

AuthenticatedClient
  -> service/client identity
  -> OperationActorType.SERVICE
```

服务身份不能伪装成用户，否则审计、权限和自动填充会丢失真实调用类型。

## 4. 核心对象角色

### 4.1 `AuthenticatedPrincipal`

统一主体契约，提供主体类型、稳定标识、展示名、tenantId、roles 和 permissions 快照。

### 4.2 `SecurityContext`

保存当前线程主体。打开 scope 时同时通过 `SecurityOperationContextAdapter` 建立 core OperationContext。

### 4.3 `SecurityContextScope`

保存旧主体与旧 OperationContext，close 时恢复。它支持嵌套调用，不应被简单的 `clear()` 逻辑替代。

### 4.4 `PermissionChecker`

Service 层稳定权限入口：

- `requireUser()`：必须是用户主体。
- `has(permission)`：返回判断结果。
- `require(permission)`：失败抛认证或授权异常。

默认实现只检查当前主体 permissions 快照，不查询角色表。

### 4.5 trusted-header 协议组件

负责 canonicalize、HMAC、timestamp 和 Header 主体解析，但 Servlet 生命周期入口属于 `synapse-security-webmvc`。

## 5. 主链路

```text
Trusted Header / OAuth2 adapter
  -> AuthenticatedPrincipal
  -> SecurityContext.openScope
  -> SecurityOperationContextAdapter
  -> OperationContext
  -> Service / PermissionChecker / data / audit
  -> scope.close
```

## 6. 身份权威边界

同一个请求应只有一个权威身份来源。trusted-header 与 OAuth2 Resource Server 同时启用可能产生主体覆盖或歧义，因此默认应 fail-fast，除非消费方明确设计优先级。

trusted-header 只适用于网络隔离和入口控制已经建立的可信链路；HMAC 不能替代完整网络和重放防护。

## 7. 生命周期与失败边界

- Servlet 线程池结束时必须关闭 SecurityContext Scope。
- Async / Task / MQ 不会自动继承 ThreadLocal，应显式恢复上下文。
- 无主体时 `require` 应抛未认证，而不是权限不足。
- 有主体但无权限时应抛 403 语义异常。
- permissions 为空不能通过 role 在 Framework 中临时推导。
- 密码编码器只处理 hash，不承载登录限流和账户锁定。

## 8. 扩展原则

- ABAC / 远程权限校验：替换 `PermissionChecker`。
- 自定义 Web 入口：将外部身份转换为 `AuthenticatedPrincipal` 后打开 scope。
- security 不向 data 暴露 LoginUser；data 只读取 OperationContext。
- 新主体类型必须同步定义其 OperationActor 映射和审计语义。

## 9. 源码阅读顺序

```text
AuthenticatedPrincipal
  -> AuthenticatedUser / AuthenticatedClient
  -> SecurityContextScope
  -> SecurityContext
  -> SecurityOperationContextAdapter
  -> PermissionChecker
  -> DefaultPermissionChecker
  -> RequirePermissionAspect
  -> trusted-header protocol classes
  -> AutoConfiguration
```

## 10. 手写练习

1. 创建 USER 主体并打开 scope。
2. 验证 `currentUser` 和 OperationContext actor。
3. 嵌套 CLIENT scope，关闭后恢复 USER。
4. 写 `PermissionChecker` 测试区分 401 与 403。

## 11. 修改检查清单

- 是否依赖 Spring Security Web / Config。
- 是否把 CLIENT 当成 USER。
- 是否把用户表、角色表或菜单模型放入 Framework。
- 是否忘记关闭 ThreadLocal scope。
- 是否让 data 反向依赖 security。
- 是否把 AOP 当成唯一安全边界。
- trusted-header 是否被描述为可替代 Bearer Token。
