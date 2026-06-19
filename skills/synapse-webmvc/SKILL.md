# synapse-webmvc Skill

## 模块定位

`synapse-webmvc` 是 Synapse Framework 的 Servlet MVC 技术支撑模块。

允许提供：

- 复用 `synapse-web-core` 的 `Result` 响应结构。
- MVC 全局异常处理。
- Servlet Filter 阶段异常桥接。
- MVC traceId / RequestContext。
- 复用 `synapse-web-core` 的 Jackson Module 和 Builder Customizer。
- OpenAPI visibility 策略。
- MVC OperationContext 恢复扩展。

禁止提供：

- WebFlux / Reactor 能力。
- Gateway 路由。
- Gateway 鉴权业务。
- 可启动 Gateway。
- 业务 Controller。
- 业务 Service / Entity / Mapper / Repository。

## 开发前必读

- `AGENTS.md`
- `docs/phase-2/00-framework-boundary.md`
- `docs/phase-2/03-boundary-checklist.md`
- `docs/modules/synapse-webmvc.md`

## 标准实现模式

- 自动配置必须使用条件装配，不能覆盖消费方 Bean。
- 不得创建全局 `ObjectMapper` Bean。
- Servlet 相关能力必须只在 Servlet Web Application 下生效。
- Filter 阶段异常桥接必须早于安全 Filter。
- `synapse-webmvc` 不依赖 `synapse-security`。

## 测试要求

- `Result` 行为测试。
- MVC 全局异常测试。
- Servlet Filter 异常桥接测试。
- Trace Filter 测试。
- 自动配置启用、关闭、缺类不误装配测试。

## 常见错误

- 把业务 Controller 放进 framework。
- 为了复用让 WebFlux 依赖 `synapse-webmvc`。
- 把 Gateway 能力写入 MVC 模块。
