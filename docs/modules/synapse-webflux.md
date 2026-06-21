# synapse-webflux 使用手册

## 1. 模块定位

`synapse-webflux` 是 Synapse Framework 的 WebFlux 技术支撑模块。

它只提供 reactive Web 基础设施：

- WebFlux 请求 traceId / requestId 处理。
- Reactor Context 中的不可信请求技术上下文。
- WebFlux 异常响应适配。
- 复用 `synapse-web-core` 的 JSON Builder 定制。
- 复用 `synapse-web-core` 的统一 `Result` 响应结构。

`synapse-webflux` 不是 Gateway 服务，不提供路由、鉴权业务、限流后台或可启动应用。

## 2. 当前事实

当前模块已经进入 root `pom.xml` reactor。

当前提供：

- `SynapseWebFluxContextFilter`
- `ReactiveRequestContext`
- `SynapseWebFluxExceptionHandler`
- `WebFluxExceptionResponseFactory`
- `com.indigo.synapse.web.core.response.Result`
- `SynapseWebCoreAutoConfiguration`
- `SynapseWebFluxAutoConfiguration`

## 3. 适用场景

- WebFlux 应用需要统一 traceId / requestId。
- WebFlux 应用需要统一异常 JSON 响应。
- Platform Gateway 需要引用 Framework 的 WebFlux 技术支撑能力。
- WebFlux 链路需要传播 traceId、requestId 和真实传输入口信息。

## 4. 不适用场景

`synapse-webflux` 不承担以下职责：

- Gateway 启动服务。
- Gateway 路由配置。
- Gateway 鉴权业务。
- Gateway 限流后台。
- 业务 Controller。
- IAM 登录认证。
- 消息中心、配置中心、文件中心等平台服务。

## 5. Maven 引入

```xml
<dependency>
    <groupId>com.indigo.synapse</groupId>
    <artifactId>synapse-webflux</artifactId>
</dependency>
```

该模块可以依赖 `spring-webflux`，但不得依赖 `spring-webmvc` 或 `jakarta.servlet`。

## 6. 核心链路

```text
WebFlux Request
  -> SynapseWebFluxContextFilter
  -> traceId / requestId response headers
  -> Reactor Context
  -> Handler / Gateway technical chain
```

异常链路：

```text
WebFlux Throwable
  -> SynapseWebFluxExceptionHandler
  -> WebFluxExceptionResponseFactory
  -> Result JSON
```

## 7. 请求上下文信任边界

- 普通 Header 只允许提供 traceId、requestId 等不可信技术字段。
- method、path、clientIp 等来源信息由当前 WebFlux Adapter 根据真实请求建立。
- actor、tenant、initiator、roles、permissions 不进入 `ReactiveRequestContext`。
- 已认证 actor 和 tenant 由 Reactive Resource Server 在完成 Token 验证后写入独立的可信 Reactor Context。
- 当前没有可信内部 initiator 协议时，initiator 默认等于 actor。

## 8. 边界与注意事项

### 8.1 不要做 Gateway 服务

Gateway 可启动服务属于 Synapse Platform。Framework 中的 `synapse-webflux` 只能提供 WebFlux 技术支撑。

### 8.2 不要引入 MVC / Servlet

`synapse-webflux` 禁止引入：

- `spring-webmvc`
- `jakarta.servlet`
- `DispatcherServlet`
- Servlet Filter

### 8.3 Reactor Context 是主通道

WebFlux 场景不要依赖 Servlet ThreadLocal。请求技术上下文、traceId、requestId 应通过 Reactor Context 读取；
认证 `OperationContext` 通过 Resource Server 的可信读取入口获取。

## 9. 常见问题

### Q1：`synapse-webflux` 是否能被 `synapse-gateway` 引用？

可以。`synapse-gateway` 属于 Synapse Platform，可引用 `synapse-webflux` 的技术能力。但 Gateway 路由、鉴权和启动服务不得进入 Framework。

### Q2：MVC 和 WebFlux 是否共用 `Result`？

是。两者都依赖 `synapse-web-core` 的 `com.indigo.synapse.web.core.response.Result`，
因此 WebFlux 不需要依赖 MVC 或 Servlet。
