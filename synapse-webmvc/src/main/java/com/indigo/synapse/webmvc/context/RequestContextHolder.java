package com.indigo.synapse.webmvc.context;

import java.util.Optional;

/**
 * 当前线程请求上下文持有器。
 *
 * <p>该类型只服务于 Servlet MVC 请求处理链路，用于在一次请求内共享 traceId、method、path、clientIp。
 * 它不是 OperationContext，也不是安全上下文。请求结束后必须清理，避免 Servlet 线程复用造成污染。</p>
 */
public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> REQUEST_CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
    }

    /**
     * 设置当前请求上下文；传入 null 时清理上下文。
     */
    public static void set(RequestContext requestContext) {
        if (requestContext == null) {
            clear();
            return;
        }
        REQUEST_CONTEXT.set(requestContext);
    }

    /**
     * 返回当前请求上下文。
     */
    public static Optional<RequestContext> current() {
        return Optional.ofNullable(REQUEST_CONTEXT.get());
    }

    /**
     * 清理当前请求上下文。
     */
    public static void clear() {
        REQUEST_CONTEXT.remove();
    }
}
