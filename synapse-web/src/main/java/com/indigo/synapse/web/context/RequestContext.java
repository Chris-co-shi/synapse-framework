package com.indigo.synapse.web.context;

public record RequestContext(String traceId, String method, String path, String clientIp) {

    public RequestContext {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
    }
}
