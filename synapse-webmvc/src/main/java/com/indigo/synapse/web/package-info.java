/**
 * Servlet MVC 技术适配层。
 *
 * <p>该模块负责统一 {@code Result}、MVC 异常处理、Filter 阶段异常桥接、trace/request
 * 生命周期、OperationContext Header 恢复和默认 JSON 规则。它只处理 Servlet MVC，不能包含
 * WebFlux、Gateway、Spring Security FilterChain 或业务 Controller。</p>
 *
 * <p>Filter 阶段异常和 DispatcherServlet 内部异常属于不同生命周期，必须分别由异常桥接 Filter
 * 和全局异常处理器接入，但应共享同一异常响应工厂。所有 ThreadLocal 与 MDC 必须在请求结束时清理。</p>
 */
package com.indigo.synapse.web;
