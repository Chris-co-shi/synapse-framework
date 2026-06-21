package com.indigo.synapse.webmvc.trace;

import com.indigo.synapse.web.core.trace.TraceIdResolver;
import com.indigo.synapse.webmvc.context.RequestContext;
import com.indigo.synapse.webmvc.context.RequestContextHolder;

/**
 * Web trace 生命周期工具。
 *
 * <p>该类型集中处理一次 MVC 请求开始和结束时的 traceId、MDC、RequestContext 设置与清理。
 * 调用方应保证 start 和 end 成对出现，避免 Servlet 线程复用时残留旧请求信息。</p>
 */
public final class WebTraceLifecycle {

    private WebTraceLifecycle() {
    }

    /**
     * 开始一次 Web trace 生命周期。
     */
    public static RequestContext start(
            String incomingTraceId,
            String incomingRequestId,
            String method,
            String path,
            String clientIp
    ) {
        String traceId = TraceIdResolver.resolve(incomingTraceId);
        String requestId = TraceIdResolver.resolve(incomingRequestId);
        TraceContext.setTraceId(traceId);
        TraceMdc.setTraceId(traceId);
        RequestContext requestContext = new RequestContext(traceId, requestId, method, path, clientIp);
        RequestContextHolder.set(requestContext);
        return requestContext;
    }

    /**
     * 结束一次 Web trace 生命周期并清理上下文。
     */
    public static void end() {
        RequestContextHolder.clear();
        TraceContext.clear();
        TraceMdc.clear();
    }
}
