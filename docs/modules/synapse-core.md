# synapse-core 使用手册

## 1. 模块定位

`synapse-core` 是 Synapse Framework 的最底层模块。

它只提供所有技术模块都可以复用的核心契约，不依赖任何其他 `synapse-*` 模块。

当前核心能力包括：

- 通用错误码契约。
- 通用异常模型。
- 认证 / 授权基础异常。
- OperationContext 操作上下文。
- OperationActor 操作主体。
- OperationSource 操作来源。
- OperationContextHolder / Scope / Snapshot。
- OperationContextProvider 读取端口。
- ID 生成抽象与 UUID 默认实现。

## 2. 适用场景

业务系统或平台系统在以下场景应引入 `synapse-core`：

- 需要统一错误码和异常模型。
- 需要使用 `OperationContext` 承载当前操作人、来源、traceId、requestId 等技术上下文。
- 需要在 HTTP、MQ、Task、Async、内部调用之间传递操作上下文。
- 需要让 data、audit、mq、security 等模块共享同一套上下文抽象。
- 需要一个不依赖 Redis、数据库、Web、Security 的基础 ID 生成抽象。

## 3. 不适用场景

`synapse-core` 不负责：

- Web 响应模型。
- Controller 异常处理。
- 认证登录。
- 用户、角色、菜单、组织等业务模型。
- OAuth2 / JWT / JWK 实现。
- 数据库访问。
- Redis 缓存。
- MQ 消息适配。
- 多租户规则。
- ABAC / DataScope。

如果某个能力需要依赖具体基础设施或业务语义，就不应该放在 `synapse-core`。

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

再按需引入 core：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-core</artifactId>
</dependency>
```

## 5. 核心能力

### 5.1 错误码

核心接口：

```java
ErrorCode
CommonErrorCode
```

`ErrorCode` 是稳定错误语义契约，只包含：

```java
String code();
String message();
```

`CommonErrorCode` 只定义跨模块通用错误码，例如：

- `SUCCESS`
- `COMMON_BAD_REQUEST`
- `COMMON_UNAUTHORIZED`
- `COMMON_FORBIDDEN`
- `COMMON_NOT_FOUND`
- `COMMON_METHOD_NOT_ALLOWED`
- `COMMON_UNSUPPORTED_MEDIA_TYPE`
- `COMMON_CONFLICT`
- `COMMON_INTERNAL_ERROR`

模块细分错误码不应放入 core。例如 security 的签名错误、trusted-header 过期、权限不足细分码应留在 `synapse-security`。

### 5.2 通用异常

核心异常：

```java
SynapseException
SynapseAuthenticationException
SynapseAccessDeniedException
```

默认语义：

```text
SynapseAuthenticationException() -> COMMON_UNAUTHORIZED
SynapseAccessDeniedException()   -> COMMON_FORBIDDEN
```

上层模块可以显式传入自己的 `ErrorCode` 实现，但 core 不感知上层模块。

### 5.3 OperationContext

核心类型：

```java
OperationContext
OperationActor
OperationActorType
OperationSource
OperationContextHolder
OperationContextScope
OperationContextSnapshot
OperationContextProvider
DefaultOperationContextProvider
```

`OperationContext` 用于表达当前操作的技术上下文：

- 谁在操作：`actor`
- 谁最初发起：`initiator`
- 从哪里来：`source`
- 如何追踪：`traceId` / `requestId`
- 何时发生：`occurredAt`
- 扩展信息：`attributes`

它不是业务用户模型，也不是 security 上下文。

### 5.4 ID 生成

核心类型：

```java
IdGenerator
UuidIdGenerator
```

`UuidIdGenerator` 返回 32 位无连字符 UUID 字符串，适合 trace、request、一次性标识等通用场景。

不适合：

- 需要严格递增的数据库主键。
- 需要全局发号服务的业务单号。
- 需要 Redis / DB 协调的 ID 生成策略。

这些应由上层模块或业务系统自行实现 `IdGenerator`。

## 6. 快速使用

### 6.1 抛出统一异常

```java
throw new SynapseException(CommonErrorCode.COMMON_BAD_REQUEST, "参数不合法");
```

认证失败：

```java
throw new SynapseAuthenticationException();
```

授权失败：

```java
throw new SynapseAccessDeniedException();
```

### 6.2 建立 OperationContext

```java
OperationActor actor = new OperationActor(
        OperationActorType.USER,
        "10001",
        "张三",
        null,
        Map.of()
);

OperationSource source = new OperationSource(
        "HTTP",
        "order-service",
        "instance-1",
        "/api/sample",
        Map.of()
);

OperationContext context = new OperationContext(
        actor,
        actor,
        source,
        "trace-001",
        null,
        "request-001",
        Instant.now(),
        Map.of()
);
```

### 6.3 使用作用域自动恢复上下文

```java
try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
    // 在这里执行 data / audit / message 等需要上下文的逻辑
}
```

作用域关闭后，进入作用域前的上下文会被恢复，避免线程复用污染。

### 6.4 快照和恢复

```java
OperationContextSnapshot snapshot = OperationContextHolder.snapshot();

try (OperationContextScope ignored = OperationContextHolder.restore(snapshot)) {
    // 异步任务、消息消费或补偿逻辑
}
```

快照只保存上下文对象，不负责序列化为 HTTP Header 或 MQ Header。

## 7. 扩展方式

### 7.1 替换 OperationContextProvider

业务系统或平台系统可以提供自己的 `OperationContextProvider`，例如从自定义上下文容器读取当前操作信息。

```java
@Bean
OperationContextProvider operationContextProvider() {
    return new CustomOperationContextProvider();
}
```

前提是不能让 core 反向依赖业务系统或其他上层模块。

### 7.2 自定义 ErrorCode

模块可以定义自己的错误码枚举：

```java
public enum CustomErrorCode implements ErrorCode {
    CUSTOM_ERROR("CUSTOM_ERROR", "自定义错误");

    private final String code;
    private final String message;

    CustomErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
```

再通过 `SynapseException` 携带：

```java
throw new SynapseException(CustomErrorCode.CUSTOM_ERROR);
```

## 8. 配置项

`synapse-core` 当前没有 Spring Boot 配置项，也没有自动配置。

它是纯 Java 核心契约模块。

## 9. 边界与注意事项

### 9.1 不要把业务模型塞进 core

不要在 core 增加：

- User
- Role
- Menu
- Department
- Order
- Workflow
- TenantRule
- DataScopeRule

### 9.2 不要默认使用 system 兜底

如果没有上下文，不要在 core 自动创建 system actor。

正确方式是由入口方显式创建上下文：

- HTTP：由 web/security 建立。
- MQ：由 message 消费入口恢复。
- Task：由任务调度入口显式指定 actor。
- Async：由调用方传递 snapshot。

### 9.3 不要在 core 绑定具体基础设施

core 不应依赖：

- Servlet API
- Spring MVC
- Spring Security
- Redis
- MyBatis
- MQ SDK
- OSS SDK

## 10. 常见问题

### Q1：OperationContext 和 SecurityContext 有什么区别？

`SecurityContext` 表示当前安全主体，属于 security 模块。

`OperationContext` 表示当前操作链路的通用技术上下文，属于 core 模块。

security 可以把 `AuthenticatedUser` 适配成 `OperationActor` 放入 `OperationContext`，但 core 不依赖 security。

### Q2：OperationActor 是否等于用户？

不是。

它可以表示用户，也可以表示服务、任务、消息消费者、匿名调用方或未知主体。

### Q3：tenantId 为什么在 core 里？

一阶段不实现多租户，但 `tenantId` 是跨模块上下文传播常见字段。core 只提供承载位，不实现租户规则、租户表或数据隔离。

### Q4：为什么 ErrorCode 不包含 HTTP status？

core 不应该绑定 Web。HTTP 状态码由 `synapse-webmvc` 的状态解析器处理。

### Q5：业务系统能不能直接使用 OperationContextHolder？

可以，但要注意作用域清理。推荐使用 try-with-resources：

```java
try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
    // do something
}
```

不要在线程池任务中只 set 不 clear。
