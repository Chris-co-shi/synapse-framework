package com.indigo.synapse.web.trace;

import org.slf4j.MDC;

import java.util.Optional;

public final class TraceMdc {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String REACTOR_TRACE_ID_KEY = TraceMdc.class.getName() + ".traceId";

    private TraceMdc() {
    }

    public static void setTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            clear();
            return;
        }
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static Optional<String> currentTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID_KEY));
    }

    public static void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}
