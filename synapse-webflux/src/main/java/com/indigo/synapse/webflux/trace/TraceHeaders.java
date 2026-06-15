package com.indigo.synapse.webflux.trace;

/**
 * WebFlux trace 相关请求头约定。
 */
public final class TraceHeaders {

    public static final String TRACE_ID = "X-Trace-Id";

    public static final String REQUEST_ID = "X-Request-Id";

    private TraceHeaders() {
    }
}
