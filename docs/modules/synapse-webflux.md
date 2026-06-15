# synapse-webflux 使用手册

## 1. 模块定位

`synapse-webflux` 是 Synapse Framework 的 WebFlux 技术支撑模块。

它只提供 reactive Web 基础设施：

- WebFlux 请求 traceId / requestId 处理。
- Reactor Context 中的请求上下文。
- OperationContext Header 恢复为 `OperationContextSnapshot`。
- WebFlux 异常响应适配。
- 默认 JSON 序列化规则。
- 统一 `Result` 响应结构。

`synapse-webflux` 不是 Gateway 服务，不提供路由、鉴权业务、限流后台或可启动应用。

## 2. 当前事实

当前模块已经进入 root `pom.xml` reactor。

当前提供：

- `SynapseWebFluxContextFilter`
- `ReactiveRequestContext`
- `OperationContextWebFluxCodec`
- `SynapseWebFluxExceptionHandler`
- `WebFluxExceptionResponseFactory`
- `Result`
- `SynapseObjectMapperFactory`
- `SynapseWebFluxAutoConfiguration`

## 3. 适用场景

- WebFlux 应用需要统一 traceId / requestId。
- WebFlux 应用需要统一异常 JSON 响应。
- Platform Gateway 需要引用 Framework 的 WebFlux 技术支撑能力。
- WebFlux 链路需要从 Header 恢复 OperationContext 技术上下文。

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
  -> OperationContextSnapshot
  -> Handler / Gateway technical chain
```

异常链路：

```text
WebFlux Throwable
  -> SynapseWebFluxExceptionHandler
  -> WebFluxExceptionResponseFactory
  -> Result JSON
```

## 7. OperationContext Header

当前支持的技术 Header：

```text
X-Synapse-Actor-Type
X-Synapse-Actor-Id
X-Synapse-Actor-Name
X-Synapse-Initiator-Type
X-Synapse-Initiator-Id
X-Synapse-Initiator-Name
X-Synapse-Tenant-Id
X-Synapse-Source-Name
X-Synapse-Source-Instance-Id
```

说明：

- Header 恢复只做技术上下文恢复。
- Header 是否可信、是否签名、是否来自 Gateway 不在本模块内判定。
- `synapse-webflux` 不做认证和授权。

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

WebFlux 场景不要依赖 Servlet ThreadLocal。请求上下文、traceId、requestId、OperationContextSnapshot 应通过 Reactor Context 读取。

## 9. 常见问题

### Q1：`synapse-webflux` 是否能被 `synapse-gateway` 引用？

可以。`synapse-gateway` 属于 Synapse Platform，可引用 `synapse-webflux` 的技术能力。但 Gateway 路由、鉴权和启动服务不得进入 Framework。

### Q2：为什么不复用 `synapse-webmvc` 的 `Result`？

为了避免 WebFlux 模块间接依赖 MVC / Servlet。当前两个模块各自拥有响应结构，后续如果重复成本变高，再评估独立公共模块。
