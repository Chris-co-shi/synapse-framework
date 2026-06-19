# synapse-webflux Skill

## 模块定位

`synapse-webflux` 是 Synapse Framework 的 WebFlux 技术支撑模块，不是 Gateway 服务。

允许提供：

- WebFlux `WebFilter`。
- `ServerWebExchange` Header 解析。
- traceId / requestId 处理。
- Reactor Context 适配。
- WebFlux 异常响应适配。
- OperationContext 恢复为 reactive 上下文。
- WebFlux `Result` 写出工具。
- 复用 `synapse-web-core` 的响应模型、状态解析和 Jackson 定制。

禁止提供：

- `spring-webmvc`。
- `jakarta.servlet`。
- `DispatcherServlet`。
- Servlet Filter。
- Gateway route locator。
- Gateway filter 业务逻辑。
- Gateway 启动服务。
- 业务 Controller。

## 开发前必读

- `AGENTS.md`
- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/modules/synapse-webflux.md`

## 标准实现模式

- 自动配置必须只在 Reactive Web Application 下生效。
- 缺少 WebFlux 类时不得误装配。
- 消费方自定义 Bean 时默认 Bean 不覆盖。
- 不得创建全局 `ObjectMapper` Bean。
- 上下文传递以 Reactor Context 为主，不能依赖 Servlet ThreadLocal。
- Header 恢复只做技术上下文恢复，不做认证、授权或 Gateway 业务判定。

## 测试要求

- WebFilter traceId / requestId 测试。
- WebFlux 异常响应测试。
- Reactor Context / OperationContext 恢复测试。
- 自动配置条件装配测试。
- 依赖边界检查：不得出现 `spring-webmvc`、`jakarta.servlet`。

## 常见错误

- 把 `synapse-webflux` 做成 Gateway。
- 在 WebFlux 模块中引入 Servlet / MVC 依赖。
- 把可信 Header 校验、登录认证或网关鉴权写进本模块。
