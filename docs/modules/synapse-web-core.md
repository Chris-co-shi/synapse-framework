# synapse-web-core 使用手册

## 模块定位

`synapse-web-core` 是 Web 技术栈无关的共享契约模块，位于 `synapse-web` 聚合下。

## 当前能力

- `Result<T>` 统一响应模型。
- `ErrorHttpStatusResolver`、`CommonErrorHttpStatusResolver`、`CompositeErrorHttpStatusResolver`。
- `TraceHeaders`、`TraceIdGenerator`、`TraceIdResolver`。
- `SynapseWebCoreAutoConfiguration`。
- `JavaTimeModule` 条件装配。
- `SynapseWebJacksonCustomizer`。

JSON 自动配置只提供 Jackson `Module` 和 `Jackson2ObjectMapperBuilderCustomizer`，不创建
`ObjectMapper` Bean。Spring Boot 的 `spring.jackson.*`、用户自定义 `ObjectMapper`、用户
`Module` 和用户 `Jackson2ObjectMapperBuilderCustomizer` 均可继续生效。

## 边界

- 不得依赖 Servlet API、Spring MVC、Reactor 或 Spring WebFlux。
- 不提供 Controller、可启动应用或 Gateway 能力。
- 消费方通常直接依赖 `synapse-webmvc` 或 `synapse-webflux`。
- WebMVC 和 WebFlux 不得复制 `Result`、状态解析器或 traceId 基础规则。
