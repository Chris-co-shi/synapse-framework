# synapse-webmvc 使用手册

## 1. 模块定位

`synapse-webmvc` 是 Synapse Framework 的 Servlet MVC Web 基础模块。

它负责提供业务系统常用的 Web 层统一能力：

- 统一响应模型。
- MVC 全局异常处理。
- Filter 阶段异常桥接。
- 错误码到 HTTP 状态码映射。
- Web traceId 和请求上下文。
- 默认 JSON 序列化规则。
- OpenAPI 可见性基础策略。

`synapse-webmvc` 只支持 Servlet MVC，不包含 WebFlux / Gateway。

## 2. 适用场景

业务系统或平台系统在以下场景可以引入 `synapse-webmvc`：

- 需要统一接口响应结构。
- 需要统一处理 `SynapseException`。
- 需要将常见 MVC 请求错误映射为 400 / 404 / 405 / 415 / 500。
- 需要 Filter 阶段的 `SynapseException` 返回统一 JSON 响应。
- 需要请求 traceId 头、MDC 和响应体 traceId 保持一致。
- 需要在 Spring Boot Jackson 构建链上应用 Framework 默认规则。

## 3. 不适用场景

`synapse-webmvc` 不适合承担以下职责：

- WebFlux。
- Spring Cloud Gateway。
- Gateway 路由。
- Gateway 鉴权。
- 业务 Controller。
- 业务 API 聚合。
- Spring Security FilterChain。
- 登录认证流程。
- 业务错误码定义。
- 业务响应 DTO。

Gateway 可启动服务属于 Platform；WebFlux 技术支撑由 `synapse-webflux` 承担。

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

再引入 web 模块：

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webmvc</artifactId>
</dependency>
```

## 5. 核心能力

### 5.1 统一响应模型

核心类型：

```java
Result<T>
```

响应字段：

```text
code
message
data
traceId
timestamp
```

创建成功响应：

```java
Result.success();
Result.success(data);
```

创建失败响应：

```java
Result.fail(CommonErrorCode.COMMON_BAD_REQUEST);
Result.fail(errorCode, "自定义错误消息");
```

`Result` 是 record，不是 JavaBean。Java 代码中访问字段使用：

```java
result.code();
result.message();
result.data();
result.traceId();
result.timestamp();
```

### 5.2 MVC 全局异常处理

核心类型：

```java
GlobalExceptionHandler
WebExceptionResponseFactory
WebErrorResponse
```

支持处理：

- `SynapseException`
- `MethodArgumentNotValidException`
- `BindException`
- `MissingServletRequestParameterException`
- `MethodArgumentTypeMismatchException`
- `ConstraintViolationException`
- `NoHandlerFoundException`
- `NoResourceFoundException`
- `HttpMessageNotReadableException`
- `HttpRequestMethodNotSupportedException`
- `HttpMediaTypeNotSupportedException`
- `Exception`

常见映射：

| 异常场景 | HTTP 状态 | 错误码 |
| --- | --- | --- |
| 参数错误 / JSON 错误 | 400 | `COMMON_BAD_REQUEST` |
| 未认证 | 401 | `COMMON_UNAUTHORIZED` 或上层细分码 |
| 无权限 | 403 | `COMMON_FORBIDDEN` 或上层细分码 |
| 路径不存在 / 资源不存在 | 404 | `COMMON_NOT_FOUND` |
| 方法不支持 | 405 | `COMMON_METHOD_NOT_ALLOWED` |
| Content-Type 不支持 | 415 | `COMMON_UNSUPPORTED_MEDIA_TYPE` |
| 未识别异常 | 500 | `COMMON_INTERNAL_ERROR` |

### 5.3 Filter 阶段异常桥接

核心类型：

```java
SynapseExceptionBridgeFilter
```

作用：

```text
Servlet Filter 阶段抛出 SynapseException
  -> SynapseExceptionBridgeFilter 捕获
  -> WebExceptionResponseFactory 转换
  -> Result JSON 响应
```

它解决的问题是：

```text
@RestControllerAdvice 只能处理进入 DispatcherServlet 后的异常。
Filter 阶段异常不会自动进入 MVC 全局异常处理器。
```

默认顺序：

```java
SynapseExceptionBridgeFilter.ORDER = -200
```

该顺序应早于后续业务或认证 Filter，才能包住后续 filter chain 中抛出的 Framework 异常。

### 5.4 HTTP 状态码解析

核心类型：

```java
ErrorHttpStatusResolver
CommonErrorHttpStatusResolver
CompositeErrorHttpStatusResolver
```

`core` 的 `ErrorCode` 不包含 HTTP 状态码。`synapse-webmvc` 通过 resolver 体系完成状态码映射。

业务系统如果定义了自己的错误码，并希望指定 HTTP 状态码，可以额外注册 `ErrorHttpStatusResolver`。

### 5.5 Web Trace

核心类型：

```java
TraceHeaders
TraceIdGenerator
TraceIdResolver
TraceContext
TraceMdc
MvcTraceFilter
WebTraceLifecycle
RequestContext
RequestContextHolder
```

默认请求头：

```text
X-Trace-Id
```

处理流程：

```text
HTTP Request
  -> MvcTraceFilter
  -> TraceIdResolver
  -> TraceContext
  -> TraceMdc
  -> RequestContextHolder
  -> response header X-Trace-Id
```

说明：

- 如果请求头没有 traceId，会自动生成。
- 如果请求头 traceId 格式不合法，会自动生成。
- 请求结束后会清理 `TraceContext`、`TraceMdc`、`RequestContextHolder`。

### 5.6 Jackson 定制

核心类型：

```java
SynapseWebCoreAutoConfiguration
SynapseWebJacksonCustomizer
```

默认规则：

- 注册 `JavaTimeModule`。
- 时间不写成 timestamp。
- duration 不写成 timestamp。
- 反序列化忽略未知字段。
- 保留 null 字段。
- 使用 UTC 时区。

这些能力由 `synapse-web-core` 提供。Framework 不创建全局 `ObjectMapper` Bean；
Boot `spring.jackson.*`、用户 `ObjectMapper`、用户 `Module` 和用户 Builder Customizer
均保留标准优先级。

### 5.7 OpenAPI 可见性策略

核心类型：

```java
OpenApiProperties
OpenApiVisibilityPolicy
```

默认只在以下 profile 认为 OpenAPI 可见：

```text
local
dev
test
```

该能力只提供可见性判断，不直接注册 springdoc 或 Swagger UI。

## 6. 快速使用

### 6.1 返回成功结果

```java
@GetMapping("/ping")
public Result<String> ping() {
    return Result.success("pong");
}
```

响应示例：

```json
{
  "code": "0",
  "message": "success",
  "data": "pong",
  "traceId": "...",
  "timestamp": "2026-06-14T00:00:00Z"
}
```

### 6.2 抛出框架异常

```java
throw new SynapseException(CommonErrorCode.COMMON_BAD_REQUEST, "请求参数错误");
```

`synapse-webmvc` 会转换为统一 `Result` 响应。

### 6.3 自定义错误码 HTTP 状态码

```java
@Bean
ErrorHttpStatusResolver customErrorHttpStatusResolver() {
    return errorCode -> {
        if ("CUSTOM_CONFLICT".equals(errorCode.code())) {
            return OptionalInt.of(409);
        }
        return OptionalInt.empty();
    };
}
```

## 7. 扩展方式

### 7.1 替换 ObjectMapper

```java
@Bean
ObjectMapper objectMapper() {
    return new ObjectMapper();
}
```

提供自定义 Bean 后，`synapse-webmvc` 不会创建第二个 ObjectMapper。

### 7.2 替换 Filter 阶段异常桥接

```java
@Bean
SynapseExceptionBridgeFilter synapseExceptionBridgeFilter(
        ObjectMapper objectMapper,
        WebExceptionResponseFactory responseFactory) {
    return new SynapseExceptionBridgeFilter(objectMapper, responseFactory);
}
```

通常不建议替换，除非业务系统需要特殊响应写出策略。

### 7.3 增加错误码状态解析器

通过注册 `ErrorHttpStatusResolver`，可以为业务错误码指定 HTTP 状态。

## 8. 配置项

`synapse-webmvc` 当前没有复杂外部配置项。

当前自动配置主要通过条件装配和用户 Bean 覆盖控制：

- `ObjectMapper`：用户提供则不覆盖。
- `SynapseExceptionBridgeFilter`：用户提供则不覆盖。
- `MvcTraceFilter`：用户提供则不覆盖。
- `MvcOperationContextFilter`：用户提供则不覆盖。

## 9. 边界与注意事项

### 9.1 不要把业务 Controller 放入 synapse-webmvc

`synapse-webmvc` 只提供 Web 基础设施。业务 API 必须由业务系统或平台服务拥有。

### 9.2 不要把 WebFlux 放回 synapse-webmvc

当前边界已经明确：

```text
synapse-webmvc = Servlet MVC
synapse-webflux = WebFlux 技术支撑
Gateway = Platform 可启动服务
```

### 9.3 Filter 异常和 MVC 异常不是一回事

- `GlobalExceptionHandler` 处理 MVC 阶段异常。
- `SynapseExceptionBridgeFilter` 处理 Filter 阶段 `SynapseException`。

认证、授权或其他基础设施 Filter 抛出的异常通常发生在 Filter 阶段，不能只依赖 `@RestControllerAdvice`。

### 9.4 Result 是统一响应模型，不是业务 DTO

业务系统可以直接返回 `Result<T>`，也可以在自己的 Controller 层转换。但不要把业务字段直接塞进 `Result` 结构本身。

### 9.5 OperationContext 恢复只做技术上下文

`MvcOperationContextFilter` 使用 `synapse-core` 的 `OperationContextSnapshotCodec` 从标准 Header 恢复上下文。
缺少 actor type 或 actor id 时不会恢复上下文，也不会默认创建 system actor。Header 是否可信、是否由 Gateway 注入，不在 `synapse-webmvc` 内判断。

## 10. 常见问题

### Q1：为什么 core 的 ErrorCode 不直接包含 HTTP status？

因为 core 不应该绑定 Web。HTTP 状态码由 `synapse-webmvc` 负责解析。

### Q2：为什么已经有 GlobalExceptionHandler，还需要 SynapseExceptionBridgeFilter？

因为 Filter 阶段异常发生在 DispatcherServlet 之前，不会自动进入 MVC 全局异常处理器。

### Q3：业务系统可以自定义 Result 吗？

可以，但如果希望使用 synapse-webmvc 的统一异常处理，建议保持 `Result` 响应结构一致。

### Q4：TraceContext 和 OperationContext 是什么关系？

`TraceContext` 是 Web 层 traceId 持有器，只处理 traceId。

`OperationContext` 是 core 层通用操作上下文，包含 actor、source、traceId、requestId 等更完整信息。

### Q5：OpenAPI 策略会自动启用 Swagger UI 吗？

不会。它只提供可见性判断，不负责注册 OpenAPI 端点或 UI。
