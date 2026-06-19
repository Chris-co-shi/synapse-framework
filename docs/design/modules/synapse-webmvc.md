# synapse-webmvc 设计说明

## 1. 模块使命

`synapse-webmvc` 把 core 的错误和操作上下文契约适配到 Servlet MVC，形成统一 HTTP 响应、异常收口、trace 生命周期和请求上下文恢复。

## 2. 边界

负责：

- `Result<T>` 统一响应结构。
- MVC 异常到 HTTP status / Result 的转换。
- Filter 阶段 `SynapseException` 的统一响应桥接。
- traceId、requestId、MDC 和请求上下文生命周期。
- 从标准 Header 恢复 OperationContext。
- Servlet MVC 默认 JSON 规则。

不负责：

- 业务 Controller 和 DTO。
- Spring Security FilterChain。
- OAuth2 / 身份 Header 认证。
- WebFlux / Gateway。
- 业务错误码定义。

## 3. 为什么 Filter 与 ControllerAdvice 必须分开

```text
Servlet Filter exception
  -> before DispatcherServlet
  -> cannot be handled by @RestControllerAdvice

Controller / argument resolution exception
  -> inside MVC
  -> GlobalExceptionHandler
```

因此存在两个异常入口，但二者复用同一个 `WebExceptionResponseFactory`，避免返回格式和 HTTP 映射规则分叉。

## 4. 核心对象角色

### 4.1 `Result<T>`

仅定义 API 外壳：code、message、data、traceId、timestamp。业务字段属于 `data` 内部 DTO，不应继续扩展 Result 顶层结构。

### 4.2 `WebExceptionResponseFactory`

异常语义转换核心：

```text
Throwable / ErrorCode
  -> ErrorHttpStatusResolver
  -> WebErrorResponse
  -> Result
```

Handler 和 Filter 只负责接入各自生命周期，不重复判断错误码。

### 4.3 `GlobalExceptionHandler`

处理进入 MVC 后的异常，包括参数校验、JSON 解析、类型转换、404、405、415 和未识别异常。

### 4.4 `SynapseExceptionBridgeFilter`

应位于可能抛出 SynapseException 的认证/上下文 Filter 之前，用 try/catch 包住后续 FilterChain。

### 4.5 Trace / Request Context

`MvcTraceFilter` 建立 traceId、MDC、RequestContext 和响应 Header，在 finally 中清理。`MvcOperationContextFilter` 只恢复技术上下文，不认证 Header 的可信性。

## 5. 主链路

```text
HTTP Request
  -> SynapseExceptionBridgeFilter
  -> MvcTraceFilter
  -> MvcOperationContextFilter
  -> security filters owned by other modules
  -> DispatcherServlet
  -> Controller
  -> GlobalExceptionHandler when failed
  -> Result JSON
  -> finally clean MDC / RequestContext / OperationContext
```

## 6. HTTP 状态边界

core 的 ErrorCode 不携带 HTTP status。webmvc 通过 resolver 完成映射：

- 400：请求内容或参数无效。
- 401：未认证。
- 403：已认证但无权限。
- 404：路径或资源不存在。
- 405：方法不支持。
- 415：媒体类型不支持。
- 500：未识别内部错误。

业务错误码可注册额外 resolver，不应修改 core ErrorCode 接口。

## 7. 生命周期与安全要求

- Filter 必须在 finally 中清理 ThreadLocal 和 MDC。
- 统一异常响应不得泄露完整堆栈、凭证和敏感请求内容。
- traceId 应在响应 Header、Result 和日志 MDC 中一致。
- OperationContext Header 恢复不等于信任建立；可信边界属于 Gateway / Security。
- ObjectMapper 默认使用 UTC，但业务发生地日期仍需要 time 模块显式转换。

## 8. 扩展原则

- 自定义业务错误到 HTTP 映射：新增 `ErrorHttpStatusResolver`。
- 自定义 JSON：优先提供 Jackson `Module` 或 `Jackson2ObjectMapperBuilderCustomizer`；
  需要完全接管时仍可提供自定义 `ObjectMapper` Bean。
- 自定义 Filter 时保持异常桥接和上下文清理顺序。
- 不通过覆盖整个模块来增加一个业务 Controller。

## 9. 源码阅读顺序

```text
Result
  -> ErrorHttpStatusResolver hierarchy
  -> WebExceptionResponseFactory
  -> GlobalExceptionHandler
  -> SynapseExceptionBridgeFilter
  -> MvcTraceFilter / WebTraceLifecycle
  -> MvcOperationContextFilter
  -> SynapseWebMvcAutoConfiguration
  -> tests
```

## 10. 手写练习

1. 写一个 Filter 抛出 `SynapseException`。
2. 验证 ControllerAdvice 捕获不到。
3. 增加外层 Bridge Filter 并输出 Result。
4. 在后续 Filter 再抛异常，验证 traceId 和 MDC 在请求结束后清理。

## 11. 修改检查清单

- 是否混入 WebFlux、Gateway 或业务 Controller。
- 是否让 Filter 和 ControllerAdvice 使用了两套错误映射。
- 是否把认证逻辑放进 OperationContext Filter。
- 是否在异常响应中泄露敏感信息。
- 是否忘记清理 MDC / ThreadLocal。
- 用户自定义 Bean 是否能让默认自动配置退让。
