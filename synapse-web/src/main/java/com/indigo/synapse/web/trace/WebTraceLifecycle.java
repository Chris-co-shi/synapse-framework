package com.indigo.synapse.web.trace;

import com.indigo.synapse.web.context.RequestContext;
import com.indigo.synapse.web.context.RequestContextHolder;

public final class WebTraceLifecycle {

    private WebTraceLifecycle() {
    }

    public static RequestContext start(String incomingTraceId, String method, String path, String clientIp) {
        String traceId = TraceIdResolver.resolve(incomingTraceId);
        TraceContext.setTraceId(traceId);
        TraceMdc.setTraceId(traceId);
        RequestContext requestContext = new RequestContext(traceId, method, path, clientIp);
        RequestContextHolder.set(requestContext);
        return requestContext;
    }

    public static void end() {
        RequestContextHolder.clear();
        TraceContext.clear();
        TraceMdc.clear();
    }
}
