# TASK-303 Security / OperationContext 生命周期强化

本文档记录 Phase 3 TASK-303 的最终契约、实现边界和验证结果。

## 1. 目标

TASK-303 解决 `synapse-security` 在同步线程内建立、替换、嵌套和清理安全上下文时的生命周期一致性问题，确保：

- `SecurityContext` 只向 core `OperationContext` 单向适配认证主体。
- 外层 Job、Async、MQ 或已有安全作用域可以被精确恢复。
- 异常路径、重复清理和线程复用不会泄漏用户或权限。
- trusted-header 认证失败与下游业务异常严格分离。
- 显式权限检查与注解权限检查保持相同错误语义。

## 2. SecurityContext 生命周期契约

### 2.1 `set` 是替换语义

```java
SecurityContext.set(authenticatedUser);
```

`set` 用于当前执行入口只维护一个安全主体的场景：

- 第一次 `set` 保存进入安全上下文前的 `OperationContext`。
- 后续连续 `set` 替换当前用户和当前用户对应的 `OperationContext`。
- 最后调用 `clear` 时恢复第一次 `set` 前的 `OperationContext`。
- `clear` 不会恢复被后续 `set` 替换掉的旧用户。

需要嵌套恢复旧用户时，不应连续调用 `set`，而应使用 `scope`。

### 2.2 `scope` 是嵌套语义

```java
try (SecurityContextScope ignored = SecurityContext.scope(authenticatedUser)) {
    // execute protected action
}
```

`scope` 在创建时保存：

- 外层 `AuthenticatedUser`。
- 外层 `OperationContext` 精确快照。

作用域关闭时同时恢复两者。正常返回和异常退出使用相同行为。

### 2.3 空用户作用域

```java
try (SecurityContextScope ignored = SecurityContext.scope(null)) {
    // execute as unauthenticated
}
```

`scope(null)` 表示在当前作用域内临时移除安全主体和当前安全主体派生的 `OperationContext`，退出后恢复外层状态。

该能力用于 trusted-header fail-open 等明确要求“继续请求但不得继承外层认证用户”的基础设施场景。

### 2.4 清理语义

- `clear()` 可重复调用。
- 当前线程没有 SecurityContext 绑定时，`clear()` 不修改独立存在的 Job、Async 或 MQ `OperationContext`。
- `clearIfEmpty()` 只执行防御性 ThreadLocal 清理，不清理独立 `OperationContext`。
- `SecurityContextScope.close()` 可重复调用，只有第一次关闭执行恢复。
- 作用域必须在创建它的同一线程关闭；跨线程关闭会失败，避免恢复错误线程的 ThreadLocal。

## 3. trusted-header Filter 契约

### 3.1 认证阶段与下游执行阶段分离

`TrustedHeaderAuthenticationFilter` 的处理顺序固定为：

1. 提取 trusted headers。
2. 校验时间戳。
3. 按配置校验签名。
4. 解析 `AuthenticatedUser`。
5. 建立 `SecurityContextScope`。
6. 执行一次 `FilterChain`。
7. 恢复外层上下文。

只处理步骤 1 至 4 产生的认证异常。步骤 6 中下游 Filter、Controller 或 Service 抛出的 `SynapseAuthenticationException` 必须原样向外传播，不能被当前 Filter 当作 trusted-header 认证失败处理。

这一约束保证 `FilterChain` 对每个请求最多执行一次，避免 fail-open 配置造成业务逻辑重复执行。

### 3.2 fail-fast 行为

`fail-fast=true`：

- trusted-header 认证失败立即抛出稳定的认证异常。
- 不执行下游 FilterChain。
- 不改变进入 Filter 前的 SecurityContext 和 OperationContext。

`fail-fast=false`：

- trusted-header 认证失败后继续执行一次下游 FilterChain。
- 下游执行期间不存在当前认证用户，不能继承外层 SecurityContext。
- 下游执行结束后恢复进入 Filter 前的上下文。

fail-open 只定义基础设施继续执行语义，不代表请求已认证，也不应由业务代码据此放宽权限。

## 4. trusted-header 与技术载体边界

trusted-header security 负责：

- 用户稳定标识和用户名。
- 租户字段快照。
- 角色和权限快照。
- HMAC 签名验证。
- 时间戳容忍窗口验证。

security 不负责：

- 登录、Token 签发、OAuth2/JWT/JWK。
- traceId、requestId、source 的 Web/MQ 技术传播生命周期。
- nonce 持久化和严格防重放平台。
- IAM、RBAC 数据管理、ABAC 或 DataScope。

`AuthenticatedUser` 适配为 `OperationActor` 时，不把 roles 和 permissions 写入 `OperationContext`。

## 5. 权限入口一致性

以下两个入口必须使用同一 `PermissionChecker` 契约：

```java
permissionChecker.require("sample:read");
```

```java
@RequirePermission("sample:read")
```

固定行为：

- 用户不存在：`SECURITY_UNAUTHENTICATED`。
- 用户存在但权限缺失：`SECURITY_PERMISSION_DENIED`。
- 空权限：显式 `require` 抛出 `IllegalArgumentException`；注解入口委托相同检查器处理。
- 注解 AOP 只做适配，不加载权限数据、不改变错误码。

## 6. 时间戳容忍窗口

- 时间戳使用 epoch milliseconds。
- 允许过去和未来两个方向的时间偏差。
- 与容忍窗口边界完全相等时有效。
- 零容忍窗口只接受与当前时钟完全相等的时间戳。
- 超出窗口返回 `SECURITY_TRUSTED_HEADER_EXPIRED`。
- 缺失或格式错误返回 `SECURITY_INVALID_TRUSTED_HEADER`。

## 7. 测试覆盖

TASK-303 增加或强化以下测试：

- 连续设置不同用户与最终恢复基准 OperationContext。
- SecurityContext 嵌套作用域恢复。
- 正常、异常和空用户作用域。
- 重复 clear 和重复 close。
- 跨线程错误关闭保护。
- 同一线程顺序复用无用户泄漏。
- trusted-header 成功、失败、fail-fast 和 fail-open。
- 下游认证异常不重复执行 FilterChain。
- 外层 SecurityContext 与 OperationContext 恢复。
- 显式权限与注解权限错误码一致。
- 时间戳过去、未来、精确边界和零容忍窗口。

## 8. 验证命令

```bash
mvn -B -q -pl synapse-security -am test
mvn -B -q clean test
```

GitHub Actions 使用 Java 21 执行上述两条命令。TASK-303 分支的模块测试和根工程全量测试均通过。

## 9. 边界结论

TASK-303 未引入以下能力：

- IAM 或登录服务。
- Spring Security Web / Config 完整体系。
- nonce 持久化。
- ABAC、DataScope 或业务权限模型。
- 业务 Controller、Entity、Mapper、Repository 或 migration。

`data` 和 `audit` 继续只依赖 core `OperationContext` 抽象，不依赖 security。
