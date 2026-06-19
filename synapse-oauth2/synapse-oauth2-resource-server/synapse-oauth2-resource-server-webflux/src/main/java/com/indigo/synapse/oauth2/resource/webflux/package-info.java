/**
 * Reactive OAuth2 Resource Server 技术适配层。
 *
 * <p>该模块把 Reactive JWT 认证结果转换为 Synapse USER/CLIENT 主体，并通过 Reactor Context 传播
 * SecurityContext 和 OperationContext。Reactive 链路不能依赖 Servlet ThreadLocal，denylist 等外部
 * 检查也不应阻塞 event-loop。</p>
 *
 * <p>本模块可以被 Platform Gateway 引用，但自身不是 Gateway 服务，不提供 RouteLocator、网关业务
 * Filter、路由后台、JWT 签发或完整 IAM。</p>
 */
package com.indigo.synapse.oauth2.resource.webflux;
