package com.indigo.synapse.webmvc.trace;

import java.util.Optional;

/**
 * 当前线程 traceId 持有器。
 *
 * <p>该类型只保存 Web 请求链路中的 traceId，供 {@code Result}、日志 MDC 和其他 Web 基础设施读取。
 * 它不等同于 core 的 OperationContext，也不保存操作人、租户、权限等信息。</p>
 */
public final class TraceContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceContext() {
    }

    /**
     * 设置当前 traceId；传入空值时清理。
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            TRACE_ID.remove();
            return;
        }
        TRACE_ID.set(traceId);
    }

    /**
     * 返回当前 traceId。
     */
    public static Optional<String> currentTraceId() {
        return Optional.ofNullable(TRACE_ID.get());
    }

    /**
     * 清理当前 traceId。
     */
    public static void clear() {
        TRACE_ID.remove();
    }
}
