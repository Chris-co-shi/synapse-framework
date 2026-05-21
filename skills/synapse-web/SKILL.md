---
name: synapse-web
description: Synapse Web 基础能力最佳实践。Use when Codex implements or reviews synapse-web code involving Spring MVC, WebFlux, unified API responses, global exception handling, validation errors, pagination, trace IDs, request context, or OpenAPI configuration.
---

# Synapse Web

## 必读

- `AGENTS.md`
- `docs/01-architecture.md`
- `docs/02-module-boundary.md`
- `docs/05-api-rules.md`
- `docs/07-test-rules.md`
- `docs/10-technical-foundation-baseline.md`
- `skills/synapse-common/SKILL.md`

## 职责和边界

- 提供 Spring MVC 和 WebFlux 双栈 Web 基础能力。
- 统一响应、异常处理、参数校验错误、分页、排序白名单、Trace ID、OpenAPI 配置。
- 可以依赖 `synapse-common`。
- 不依赖 Data、Security、Audit、Admin 业务模块。

## 推荐包结构

```text
com.indigo.synapse.web
├── response
├── exception
├── page
├── trace
├── context
├── openapi
└── autoconfigure
```

## 标准实现模式

- MVC 和 WebFlux 的响应结构必须一致。
- MVC 和 WebFlux 异常响应必须通过 `WebExceptionResponseFactory` 共享同一套错误码、HTTP status、响应体规则。
- 参数校验错误不能泄露内部异常栈。
- 排序字段必须走白名单映射。
- OpenAPI 开发环境默认启用，生产环境必须可关闭或受安全策略控制。
- `ApiResponse` 必须包含 `code`、`message`、`data`、`traceId`、`timestamp`。
- `ApiResponse.success/fail` 必须优先使用 `TraceContext` 中的 traceId；无上下文时生成 32 位 traceId。
- 分页请求使用 `PageRequest.of(pageNo, pageSize)` 统一归一化：`pageNo` 最小为 1，`pageSize` 默认 20、最大 200。
- 分页响应使用 `PageResponse.of(records, pageNo, pageSize, total)`，records 必须防御性复制。
- 排序使用 `SortWhitelist` 将前端字段映射为内部排序属性，未知字段必须忽略。
- Trace 基础上下文使用 `TraceContext` 保存当前线程 traceId，任务结束或测试结束必须 clear。
- Trace ID 生成使用 `TraceIdGenerator.generate()`，保持 32 位无连字符 UUID 字符串。
- 外部传入 traceId 必须通过 `TraceIdResolver` 校验，禁止接受换行、斜杠等危险字符；非法值必须重新生成。
- `WebTraceLifecycle.start(...)` 负责建立 `TraceContext` 和 `RequestContextHolder`，`end()` 必须清理二者。
- MVC Trace 适配使用 `MvcTraceFilter`，必须读取/写入 `X-Trace-Id`，并在 filter chain 结束后清理上下文。
- WebFlux Trace 适配使用 `WebFluxTraceWebFilter`，必须读取/写入 `X-Trace-Id`，并在 reactive chain finally 阶段清理上下文。
- `RequestContext` 只保存请求基础摘要：traceId、method、path、clientIp，不放认证用户、Entity 或业务对象。
- OpenAPI 可见性使用 `OpenApiVisibilityPolicy` 判断：仅 `local/dev/test` 且配置启用时可见，生产默认不可见。
- `SynapseWebAutoConfiguration` 只负责暴露不依赖具体运行时栈的 Web Foundation 基础 Bean，例如 OpenAPI 默认属性。
- MVC 与 WebFlux 自动配置必须拆分为独立类：`SynapseWebMvcAutoConfiguration`、`SynapseWebFluxAutoConfiguration`。
- MVC 自动配置必须使用 `@ConditionalOnClass(name = {...})` 通过类名判断 Servlet/MVC 栈，避免缺少 Servlet 运行时时类加载失败。
- WebFlux 自动配置必须使用 `@ConditionalOnClass(name = {...})` 通过类名判断 Reactive/WebFlux 栈，避免缺少 WebFlux 运行时时类加载失败。
- `AutoConfiguration.imports` 必须同时注册 Base、MVC、WebFlux 三个自动配置类，Starter feature switch 负责统一过滤。
- Web 模块允许依赖 `spring-web`、`spring-webmvc`、`spring-webflux`、`spring-context`、`jakarta.servlet-api`、`jakarta.validation-api`；这些依赖不得被业务模块反向污染。

## 测试要求

- 覆盖成功响应、业务异常、校验异常、未知异常。
- 覆盖分页默认值、最大 pageSize、排序白名单。
- 覆盖分页 records 防御性复制和 null records 拒绝。
- 覆盖未知排序字段不返回排序条件，防止 SQL 字段穿透。
- 覆盖 TraceContext set/current/clear 和空白 traceId 清理。
- 覆盖 `ApiResponse` 的 traceId、timestamp、无上下文自动生成 traceId。
- 覆盖 `TraceIdResolver` 对合法 traceId 的复用、非法 traceId 的重新生成。
- 覆盖 `WebTraceLifecycle` 对 TraceContext 和 RequestContextHolder 的 start/end。
- 覆盖 `MvcTraceFilter` 设置响应 trace header 并清理上下文。
- 覆盖 `WebFluxTraceWebFilter` 设置响应 trace header 并清理上下文。
- 覆盖 `OpenApiVisibilityPolicy` 在 dev/local/test/prod 下的可见性。
- 覆盖 `SynapseWebAutoConfiguration` 可创建基础 Bean。
- 覆盖 MVC/WebFlux 自动配置在对应栈可用时创建 Trace 适配 Bean。
- 覆盖缺少 Servlet/MVC 类时 MVC 自动配置不创建 Bean 且不类加载失败。
- 覆盖缺少 WebFlux 类时 WebFlux 自动配置不创建 Bean 且不类加载失败。
- 覆盖 `WebExceptionResponseFactory` 的 MVC/WebFlux 一致响应。
- MVC/WebFlux 双栈能力必须分别测试。
- 模块完成后必须运行 `/Users/sxc/Documents/tool/apache-maven-3.9.0/bin/mvn -pl synapse-web -am test`，关键变更再运行根目录 `clean test`。

## 常见错误

- 只实现 MVC，忘记 WebFlux 契约。
- 在异常响应中返回堆栈。
- 允许前端直接传数据库字段排序。
- 让分页参数在各 Controller 中各自处理，导致默认值和上限不一致。
- ThreadLocal traceId 使用后不清理，污染后续请求或测试。
- 直接信任请求头里的 traceId，导致响应头/日志注入风险。
- 在 OpenAPI 规则里默认生产可见。
- 在 RequestContext 中塞入 LoginUser、Entity 或业务对象，造成 Web 层和业务层耦合。
- MVC/WebFlux 过滤器只 set 不 clear，导致 ThreadLocal 泄漏。
- 只测试纯 Java lifecycle，不测试真实 Filter/WebFilter 适配。
- 把 MVC/WebFlux Trace Bean 放回 Base 自动配置，导致某一运行时栈缺失时启动失败。
- 用直接 class 引用做条件判断，导致 optional 运行时依赖提前类加载。

## 示例任务拆分

- 实现统一分页模型。
- 实现 Trace ID 过滤器和响应注入。
- 增加 MVC/WebFlux 全局异常处理测试。
- 增加请求上下文基础契约。
- 增加 OpenAPI 可见性策略。
- 实现 WebMVC Trace Filter。
- 实现 WebFlux Trace WebFilter。
- 实现 OpenAPI 开发环境自动配置。
- 增加 MVC/WebFlux Trace 适配测试。
- 拆分 Base/MVC/WebFlux 自动配置并补缺类场景测试。
