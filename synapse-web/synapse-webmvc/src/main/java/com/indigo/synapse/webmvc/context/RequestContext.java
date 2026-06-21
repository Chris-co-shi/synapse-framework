package com.indigo.synapse.webmvc.context;

/**
 * Servlet MVC 请求上下文。
 *
 * <p>该对象记录一次 HTTP 请求在 Web 层可复用的基础元数据。它只用于 Web 请求追踪和排查，
 * 不承载认证主体、业务用户、角色、菜单或业务参数。</p>
 *
 * @param traceId 链路追踪 ID
 * @param requestId 请求级关联标识
 * @param method HTTP 方法
 * @param path 请求路径
 * @param clientIp 客户端 IP
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
