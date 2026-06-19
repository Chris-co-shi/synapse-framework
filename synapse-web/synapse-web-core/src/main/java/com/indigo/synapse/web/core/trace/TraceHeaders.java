package com.indigo.synapse.web.core.trace;

/**
 * Web 技术栈无关的 trace 和 request 请求头约定。
 */
public final class TraceHeaders {

    public static final String TRACE_ID = "X-Trace-Id";

    /**
     * 请求级关联标识 Header。
     */
    public static final String REQUEST_ID = "X-Request-Id";

    private TraceHeaders() {
    }
}
