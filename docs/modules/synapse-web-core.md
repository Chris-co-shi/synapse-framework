# synapse-web-core 使用手册

## 模块定位

`synapse-web-core` 是 Web 技术栈无关的共享契约模块，位于 `synapse-web` 聚合下。

## 当前事实

Phase 1 仅建立可编译 JAR 和包边界，尚未迁移响应模型、错误模型或 JSON 扩展。

## 边界

- 不得依赖 Servlet API、Spring MVC、Reactor 或 Spring WebFlux。
- 不提供 Controller、可启动应用或 Gateway 能力。
- 消费方通常直接依赖 `synapse-webmvc` 或 `synapse-webflux`。
