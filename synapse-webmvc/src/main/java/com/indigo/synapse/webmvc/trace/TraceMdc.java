package com.indigo.synapse.webmvc.trace;

import org.slf4j.MDC;

import java.util.Optional;

/**
 * Web traceId 的 MDC 适配器。
 *
 * <p>该类型把当前请求 traceId 写入 SLF4J MDC，方便日志格式中输出 traceId。它只服务于 Servlet MVC
 * 同步请求链路，不负责跨线程、跨消息或 Gateway 的上下文传播。</p>
 */
public final class TraceMdc {

    /**
     * 日志 MDC 中保存 traceId 的 key。
     */
    public static final String TRACE_ID_KEY = "traceId";

    private TraceMdc() {
    }

    /**
     * 设置当前 MDC traceId；传入空值时清理。
     */
    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            clear();
            return;
        }
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 返回当前 MDC traceId。
     */
    public static Optional<String> currentTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID_KEY));
    }

    /**
     * 清理当前 MDC traceId。
     */
    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
