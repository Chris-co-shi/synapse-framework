# synapse-security 使用手册

## 1. 模块定位

`synapse-security` 是 Synapse Framework 的 Web 无关安全基础模块。

当前能力：

- `AuthenticatedPrincipal`：统一安全主体抽象。
- `AuthenticatedUser`：已认证用户主体。
- `AuthenticatedClient`：已认证客户端主体。
- `SecurityContext`：当前线程安全上下文只读门面。
- `PermissionChecker`：显式权限检查入口。
- `@RequirePermission`：声明式权限适配。
- 安全主体到 core `OperationContext` 的单向适配。
- 默认 `PasswordEncoder`。

本模块不负责从 HTTP 请求、Header 或 Token 中建立认证主体。Servlet 和 Reactive 请求的认证入口分别由 OAuth2 Resource Server 适配模块承担。

## 2. 认证边界

Synapse 当前采用 Bearer Token 作为身份权威：

```text
Authorization: Bearer <token>
  -> Resource Server 验证签名
  -> 校验 issuer / audience / expiry / token contract
  -> JWT claims 映射为 AuthenticatedUser 或 AuthenticatedClient
  -> 建立 Synapse SecurityContext
```

固定规则：

- Gateway 可以验证 Token，但下游服务仍必须独立验证。
- Gateway 与下游服务之间只传播 Bearer Token。
- 不传播或信任用户、角色、权限等身份 Header。
- `synapse-security` 不提供身份 Header 协议、HMAC Header 签名或 Servlet Filter。
- `synapse-security` 不依赖 Spring Security Web / Config。

Web 认证适配模块：

- Servlet：`synapse-oauth2-resource-server-webmvc`。
- Reactive：`synapse-oauth2-resource-server-webflux`。

## 3. 适用场景

业务系统或平台系统可以使用本模块：

- 读取当前已经过认证的用户或客户端。
- 通过 `PermissionChecker` 显式校验权限。
- 通过 `@RequirePermission` 对 Spring Bean 方法执行轻量权限检查。
- 将当前安全主体同步为 `OperationContext`，供 data、audit、mq 等模块读取。
- 使用默认 BCrypt 密码编码器。

## 4. 不适用场景

本模块不承担：

- 用户登录、用户中心或角色授权后台。
- OAuth2 Authorization Server。
- OAuth2 Resource Server。
- JWT / JWK 解析和验证。
- Spring Security `SecurityFilterChain`。
- Servlet Filter 或 WebFlux WebFilter。
- Gateway 身份 Header 注入。
- ABAC、DataScope 或多租户权限模型。

完整 IAM / RBAC / ABAC 属于 Synapse Platform。

## 5. Maven 引入

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

<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-security</artifactId>
</dependency>
```

普通 Servlet Resource Server 还应引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-oauth2-resource-server-webmvc</artifactId>
</dependency>
```

Reactive Resource Server 引入：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-oauth2-resource-server-webflux</artifactId>
</dependency>
```

## 6. 已认证主体

核心类型：

```java
AuthenticatedPrincipal
AuthenticatedUser
AuthenticatedClient
```

用户主体字段：

```text
userId
username
tenantId
roles
permissions
```

客户端主体字段：

```text
clientId
clientName
tenantId
permissions
```

约束：

- `AuthenticatedClient` 表示客户端或服务主体，不伪装成用户。
- roles 和 permissions 是当前 Token 中的安全快照。
- 本模块不查询用户、角色或权限数据源。
- CLIENT 同步到 `OperationContext` 时映射为 `OperationActorType.SERVICE`。
- roles / permissions 不进入 `OperationContext`。

## 7. SecurityContext

```java
AuthenticatedUser user = SecurityContext.currentUser()
        .orElseThrow();
```

`SecurityContext` 是面向业务代码的只读门面。

认证主体只能由 Framework 的认证适配器绑定。业务代码不得直接调用 `com.indigo.synapse.security.context.internal` 包中的 Binder、State 或 Scope。

安全主体会单向适配为 core `OperationContext`：

```text
AuthenticatedUser / AuthenticatedClient
  -> OperationActor
  -> OperationContext
```

这样 data、audit、mq 可以通过 `OperationContextProvider` 读取当前操作人，不需要依赖 security。

## 8. 权限检查

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

- 没有认证主体：抛出 `SynapseAuthenticationException(SECURITY_UNAUTHENTICATED)`。
- 没有权限：抛出 `SynapseAccessDeniedException(SECURITY_PERMISSION_DENIED)`。
- 空权限：`require` 抛出 `IllegalArgumentException`，`has` 返回 false。

## 9. 声明式权限

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
- MQ、Task、Async 场景推荐显式调用 `PermissionChecker`。

## 10. 密码编码器

默认自动配置：

```java
PasswordEncoder -> BCryptPasswordEncoder
```

该能力只依赖 `spring-security-crypto`，不引入 `spring-security-web`。

自定义 Bean 会覆盖默认 Bean：

```java
@Bean
PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

## 11. 配置项

前缀：

```yaml
synapse.security.permission
```

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `annotation-enabled` | `true` | 是否启用 `@RequirePermission` AOP 适配 |

关闭注解权限适配：

```yaml
synapse:
  security:
    permission:
      annotation-enabled: false
```

关闭后仍可显式调用 `PermissionChecker`。

## 12. 扩展 PermissionChecker

业务系统可以提供自定义 Bean：

```java
@Bean
PermissionChecker permissionChecker() {
    return new CustomPermissionChecker();
}
```

可以用于远程 IAM 权限校验、角色到权限映射或业务侧 ABAC 前置判断，但这些业务规则不应反向写入 Framework。

## 13. 注意事项

- `synapse-security` 不处理用户名密码登录、不签发 Token、不维护 Session。
- 默认 `PermissionChecker` 只检查当前主体快照中的 permissions。
- `@RequirePermission` 只适合 Spring Bean 方法。
- 异步任务不能假设 ThreadLocal 自动存在，应显式传播 `OperationContextSnapshot`。
- `OperationContext` 不承载角色和权限。

## 14. Configuration Metadata

`synapse-security` 发布 jar 必须包含 `META-INF/spring-configuration-metadata.json`，当前只公开 `synapse.security.permission.*` 配置。
