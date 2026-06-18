/**
 * Reactive Web 技术适配层。
 *
 * <p>该模块通过 Reactor Context 传播 trace、request 和 OperationContext 快照，并提供 WebFlux
 * 异常响应与 JSON 基础设施。Reactive 流可能跨线程执行，因此不能把 Servlet ThreadLocal 作为
 * 唯一上下文通道。</p>
 *
 * <p>本模块不是 Gateway 服务，不提供 RouteLocator、网关业务 Filter、路由配置或启动应用。
 * Platform Gateway 可以引用这里的技术能力，但平台运行时职责不能反向进入 Framework。</p>
 */
package com.indigo.synapse.webflux;
