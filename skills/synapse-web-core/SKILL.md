# synapse-web-core Skill

## 模块定位

`synapse-web-core` 只提供 Servlet 与 Reactive 技术栈共享的 Web 契约和默认定制。

## 允许内容

- `Result` 等不可变响应模型。
- 错误码到 HTTP 状态码的解析契约。
- traceId Header、生成和格式校验规则。
- Jackson `Module`。
- `Jackson2ObjectMapperBuilderCustomizer`。

## 禁止事项

- 不依赖 Servlet API、Spring MVC、Reactor 或 Spring WebFlux。
- 不创建全局 `ObjectMapper` Bean。
- 不提供 Controller、Filter、WebFilter、Advice 或 Gateway 能力。
- 不读取 ThreadLocal 或 Reactor Context。

## 验证

- 运行 `mvn -q -pl synapse-web/synapse-web-core -am test`。
- 检查依赖树中不存在 `jakarta.servlet-api`、`spring-webmvc`、`reactor-core` 和 `spring-webflux`。
- 验证 Boot Jackson 属性、用户 ObjectMapper、Module 和 Builder Customizer 均可共存。
