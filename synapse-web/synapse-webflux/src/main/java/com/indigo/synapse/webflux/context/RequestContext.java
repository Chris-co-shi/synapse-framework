package com.indigo.synapse.webflux.context;

/**
 * WebFlux 请求上下文。
 *
 * <p>该对象用于 Reactor Context 中保存一次 reactive HTTP 请求的技术元数据，不承载业务参数、
 * 认证主体或 Gateway 路由配置。</p>
 */
public record RequestContext(String traceId, String requestId, String method, String path, String clientIp) {

    public RequestContext {
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("method must not be blank");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("path must not be blank");
        }
    }
}
