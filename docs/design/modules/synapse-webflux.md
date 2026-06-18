# synapse-webflux 设计说明

## 1. 模块使命

`synapse-webflux` 把 trace、请求上下文、OperationContext carrier 和统一异常响应适配到 Reactive Web 执行模型。它提供 WebFlux 技术基础，但不是 Gateway 服务。

## 2. 为什么不能复用 Servlet 实现

Servlet 主要依赖线程绑定生命周期；Reactive 流可能跨线程执行。ThreadLocal 不能作为唯一上下文来源，因此 WebFlux 使用 Reactor Context。

```text
Servlet: ThreadLocal + Filter finally
Reactive: Reactor Context + publisher lifecycle
```

语义可以一致，传播机制必须分开。

## 3. 边界

负责：

- traceId / requestId 解析与响应 Header。
- `ReactiveRequestContext`。
- OperationContext Header 解码。
- Reactor Context 写入与读取。
- WebFlux 异常到 Result JSON 的转换。
- Reactive JSON 默认规则。

不负责：

- RouteLocator、Gateway Filter 业务。
- 网关认证、路由、限流后台。
- 业务 Handler / Controller。
- Servlet Filter 或 ThreadLocal 主通道。

## 4. 核心对象角色

- `SynapseWebFluxContextFilter`：请求入口，建立 trace、request 和 OperationContext snapshot。
- `ReactiveRequestContext`：从 Reactor Context 读取请求技术信息。
- `OperationContextWebFluxCodec`：WebFlux Header 与 core carrier 的适配器。
- `SynapseWebFluxExceptionHandler`：Reactive 异常写出入口。
- `WebFluxExceptionResponseFactory`：异常语义和响应模型转换。
- `SynapseWebFluxAutoConfiguration`：按 classpath 和缺失 Bean 条件装配。

## 5. 主链路

```text
ServerWebExchange
  -> SynapseWebFluxContextFilter
  -> resolve traceId / requestId
  -> decode OperationContextSnapshot
  -> contextWrite(Reactor Context)
  -> Handler / Gateway technical chain
  -> response headers
```

异常链路：

```text
Throwable
  -> SynapseWebFluxExceptionHandler
  -> WebFluxExceptionResponseFactory
  -> Result JSON
```

## 6. 生命周期与失败边界

- 不通过静态 ThreadLocal 保存 Reactive 主体。
- Context 必须通过 publisher 链传播，避免在订阅前读取。
- 缺少 actor type / id 时不创建伪 OperationContext。
- Header 恢复只代表数据解码，不代表调用方可信。
- 响应写出必须遵守 Reactive backpressure 和响应是否已提交状态。

## 7. 扩展原则

- Platform Gateway 可以依赖本模块，但 Route、鉴权和网关业务留在 Platform。
- 自定义异常响应可替换 factory / handler Bean。
- 自定义 Header 适配应继续复用 core codec 规则。
- 不为了代码复用让本模块依赖 `synapse-webmvc`。

## 8. 源码阅读顺序

```text
Result
  -> ReactiveRequestContext
  -> OperationContextWebFluxCodec
  -> SynapseWebFluxContextFilter
  -> WebFluxExceptionResponseFactory
  -> SynapseWebFluxExceptionHandler
  -> SynapseWebFluxAutoConfiguration
  -> tests with StepVerifier / WebTestClient
```

## 9. 手写练习

1. 写一个 WebFilter 将 traceId 放入 Reactor Context。
2. 在跨线程 operator 中读取该值。
3. 验证仅写 ThreadLocal 会丢失或不稳定。
4. 增加异常 handler，验证响应 traceId 与 Context 一致。

## 10. 修改检查清单

- 是否引入 `jakarta.servlet` 或 Spring MVC。
- 是否把 Gateway 运行时能力放入 Framework。
- 是否把 ThreadLocal 当成唯一上下文。
- 是否复制而不是复用 core carrier 规则。
- 是否在 response committed 后重复写响应。
- 默认 Bean 是否允许消费方覆盖。
