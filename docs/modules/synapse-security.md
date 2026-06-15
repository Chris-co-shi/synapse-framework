# synapse-security 使用手册

## 1. 模块定位

`synapse-security` 是 Synapse Framework 的轻量安全基础模块。

它不提供完整认证中心，也不创建 Spring Security FilterChain，而是提供业务服务接入可信身份上下文和权限检查所需的基础能力：

- `AuthenticatedUser` 已认证用户主体。
- `SecurityContext` 当前线程安全上下文。
- trusted-header 请求头契约。
- trusted-header HMAC 签名和时间戳校验。
- `TrustedHeaderAuthenticationFilter`。
- `PermissionChecker` 显式权限检查入口。
- `@RequirePermission` 声明式权限适配。
- security 到 core `OperationContext` 的单向适配。
- 默认 `PasswordEncoder`。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-security`：

- 服务位于 Gateway / IAM 后方，需要从可信 Header 恢复当前用户。
- 需要在业务服务中读取当前已认证用户。
- 需要通过 `PermissionChecker` 显式校验权限。
- 需要通过 `@RequirePermission` 对 Spring Bean 方法做轻量权限拦截。
- 需要把当前认证用户同步为 `OperationContext`，供 data、audit、mq 等模块使用。
- 需要一个默认 BCrypt 密码编码器。

## 3. 不适用场景

`synapse-security` 不适合承担以下职责：

- 用户登录。
- 用户中心。
- 角色授权后台。
- 菜单权限管理。
- OAuth2 Authorization Server。
- OAuth2 Resource Server。
- JWT / JWK 解析与验证。
- Spring Security `SecurityFilterChain`。
- Spring Security MethodSecurity。
- ABAC。
- DataScope。
- 多租户权限模型。

OAuth2 / JWT / JWK 技术能力属于 `synapse-oauth2`。完整 IAM / RBAC / ABAC 属于后续平台服务，不属于 framework 一阶段。

## 4. Maven 引入

推荐先引入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.indigo.synapse</groupId>
            <artifactId>synapse-bom</artifactId>
            <version>${synapse.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

再引入 security 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-security</artifactId>
</dependency>
```

如果需要 trusted-header Filter 在 Servlet Web 服务中返回统一 JSON 响应，建议同时引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

原因是 Filter 阶段异常需要 `synapse-webmvc` 的 `SynapseExceptionBridgeFilter` 统一桥接。

## 5. 核心能力

### 5.1 已认证用户主体

核心类型：

```java
AuthenticatedUser
```

字段：

```text
userId
username
tenantId
roles
permissions
```

说明：

- `userId` 和 `username` 必填。
- `roles` 和 `permissions` 是当前请求携带的快照。
- 不查询用户表。
- 不根据角色推导权限。
- 不包含菜单、组织、数据权限规则。

### 5.2 SecurityContext

核心类型：

```java
SecurityContext
```

常用方法：

```java
SecurityContext.set(authenticatedUser);
SecurityContext.currentUser();
SecurityContext.clear();
```

设置用户后，security 会把 `AuthenticatedUser` 单向适配为 core 的 `OperationContext`。

```text
AuthenticatedUser
  -> OperationActor
  -> OperationContext
```

这样 data、audit、mq 可以通过 `OperationContextProvider` 读取当前操作人，而不需要依赖 security。

### 5.3 trusted-header 契约

核心类型：

```java
SecurityHeaders
TrustedHeaderPrincipal
TrustedHeaderAuthenticatedUserResolver
TrustedHeaderCanonicalizer
TrustedHeaderSignatureVerifier
TrustedHeaderTimestampValidator
TrustedHeaderAuthenticationFilter
```

Header 名称：

```text
X-Synapse-User-Id
X-Synapse-Username
X-Synapse-Tenant-Id
X-Synapse-Roles
X-Synapse-Permissions
X-Synapse-Trace-Id
X-Synapse-Request-Id
X-Synapse-Source
X-Synapse-Signature
X-Synapse-Timestamp
X-Synapse-Nonce
```

最小必填：

```text
X-Synapse-User-Id
X-Synapse-Username
X-Synapse-Timestamp
```

如果开启签名校验，还必须提供：

```text
X-Synapse-Signature
```

### 5.4 trusted-header 签名

签名算法：

```text
HmacSHA256 + Base64
```

签名内容：

- 固定 Header 顺序。
- 缺失字段按空字符串处理。
- 不包含 `X-Synapse-Signature` 本身。

注意：

- 签名只降低 Header 被伪造的风险。
- 不替代网络隔离。
- 不替代 Gateway 访问控制。
- 不替代 nonce 存储。

### 5.5 权限检查

核心类型：

```java
PermissionChecker
DefaultPermissionChecker
```

常用方法：

```java
permissionChecker.require("resource:read");
boolean allowed = permissionChecker.has("resource:read");
AuthenticatedUser user = permissionChecker.requireUser();
```

默认行为：

- 没有用户：抛出 `SynapseAuthenticationException(SECURITY_UNAUTHENTICATED)`。
- 没有权限：抛出 `SynapseAccessDeniedException(SECURITY_PERMISSION_DENIED)`。
- 空权限：`require` 抛出 `IllegalArgumentException`，`has` 返回 false。

### 5.6 声明式权限

核心类型：

```java
@RequirePermission
RequirePermissionAspect
```

示例：

```java
@RequirePermission("sample:read")
public SampleDetail getSample(String id) {
    return sampleRepository.get(id);
}
```

说明：

- 方法级注解优先于类型级注解。
- AOP 只把注解转换为 `PermissionChecker.require(...)`。
- AOP 不是唯一安全边界。
- MQ / Task / Async 场景推荐显式调用 `PermissionChecker`。

### 5.7 密码编码器

核心类型：

```java
SynapsePasswordEncoderFactory
```

默认自动配置：

```java
PasswordEncoder -> BCryptPasswordEncoder
```

该能力只依赖 `spring-security-crypto`，不引入 `spring-security-web`。

## 6. 快速使用

### 6.1 开启 trusted-header

```yaml
synapse:
  security:
    trusted-header:
      enabled: true
      signature-enabled: true
      secret: your-shared-secret
      timestamp-tolerance: 300s
      fail-fast: true
```

Gateway / IAM 需要注入 Header：

```text
X-Synapse-User-Id: 10001
X-Synapse-Username: zhangsan
X-Synapse-Permissions: sample:read,sample:write
X-Synapse-Timestamp: 1710000000000
X-Synapse-Nonce: random-value
X-Synapse-Signature: base64-hmac-sha256
```

### 6.2 在业务代码中读取当前用户

```java
AuthenticatedUser user = SecurityContext.currentUser()
        .orElseThrow(() -> new SynapseAuthenticationException());
```

更推荐通过 `PermissionChecker` 获取：

```java
AuthenticatedUser user = permissionChecker.requireUser();
```

### 6.3 显式权限检查

```java
permissionChecker.require("sample:read");
```

### 6.4 声明式权限检查

```java
@RequirePermission("sample:read")
public SampleDetail detail(String id) {
    return sampleService.detail(id);
}
```

## 7. 扩展方式

### 7.1 替换 PermissionChecker

业务系统可以提供自定义 Bean：

```java
@Bean
PermissionChecker permissionChecker() {
    return new CustomPermissionChecker();
}
```

适用场景：

- 从 IAM 服务远程校验权限。
- 引入角色到权限的映射。
- 引入 ABAC / DataScope 前置判断。

注意：这属于业务系统或平台服务扩展，不应反向写入 framework。

### 7.2 关闭注解权限适配

```yaml
synapse:
  security:
    permission:
      annotation-enabled: false
```

关闭后仍可显式调用 `PermissionChecker`。

### 7.3 自定义 PasswordEncoder

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

用户 Bean 会覆盖默认 Bean。

## 8. 配置项

### 8.1 trusted-header

前缀：

```yaml
synapse.security.trusted-header
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `enabled` | `false` | 是否启用 trusted-header Filter |
| `signature-enabled` | `true` | 是否启用 HMAC 签名校验 |
| `secret` | 无 | HMAC 共享密钥；签名开启时必填 |
| `timestamp-tolerance` | `300s` | 时间戳容忍窗口 |
| `fail-fast` | `true` | 认证失败时是否直接抛出异常 |

### 8.2 permission

前缀：

```yaml
synapse.security.permission
```

配置项：

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `annotation-enabled` | `true` | 是否启用 `@RequirePermission` AOP 适配 |

## 9. 边界与注意事项

### 9.1 trusted-header 只能来自可信入口

业务服务不能直接信任公网客户端传入的 `X-Synapse-*` Header。

必须保证：

- Header 由 Gateway / IAM 注入。
- 下游服务不直接暴露给不可信客户端。
- 签名密钥不泄露。

### 9.2 security 不做登录

`synapse-security` 不处理用户名密码登录、不签发 token、不维护 session。

### 9.3 security 不做权限数据加载

默认 `PermissionChecker` 只检查当前用户快照中的 `permissions`。

权限数据如何加载，由 Gateway / IAM / 业务系统决定。

### 9.4 AOP 不是最终安全边界

`@RequirePermission` 只适合 Spring Bean 方法。对于 MQ、Task、Async、内部调用，推荐显式调用：

```java
permissionChecker.require("sample:read");
```

### 9.5 OperationContext 不承载角色权限

security 会把当前用户适配成 `OperationActor`，但不会把 roles / permissions 放入 `OperationContext`。

## 10. 常见问题

### Q1：为什么不用 Spring Security FilterChain？

一阶段目标是轻量恢复可信身份上下文，不做完整认证体系。完整 Spring Security / OAuth2 能力由后续 oauth2 或平台 IAM 服务处理。

### Q2：为什么 trusted-header 默认关闭？

避免业务系统只引入依赖后就强制拦截所有请求。必须显式配置开启。

### Q3：没有 Gateway 可以直接用 trusted-header 吗？

不建议。trusted-header 的前提是 Header 来自可信入口。没有 Gateway / IAM / 内网隔离时，Header 很容易被伪造。

### Q4：权限码在哪里定义？

业务系统或平台 IAM 定义。framework 只检查传入的字符串。

### Q5：如何处理异步任务中的当前用户？

异步任务不应依赖 ThreadLocal 自动存在。调用方需要显式传递 `OperationContextSnapshot`，或在任务入口手动建立 `SecurityContext` / `OperationContext`。
