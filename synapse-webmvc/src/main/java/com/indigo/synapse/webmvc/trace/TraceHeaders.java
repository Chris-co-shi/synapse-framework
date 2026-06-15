package com.indigo.synapse.webmvc.trace;

/**
 * Web trace 相关请求头约定。
 *
 * <p>一阶段只定义 Servlet MVC 请求中的 traceId 头。跨服务、MQ 或 Gateway 的完整上下文传播
 * 应由对应模块基于 OperationContext 进一步扩展。</p>
 */
public final class TraceHeaders {

    /**
     * 链路追踪请求头。
     */
    public static final String TRACE_ID = "X-Trace-Id";

    private TraceHeaders() {
    }
}
