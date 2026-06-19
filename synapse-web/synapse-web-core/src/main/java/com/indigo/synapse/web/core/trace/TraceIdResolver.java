package com.indigo.synapse.web.core.trace;

/**
 * 解析和校验外部传入的 traceId。
 *
 * <p>该规则不依赖 Servlet 或 Reactor，MVC 与 WebFlux 必须共享同一字符集和长度约束。</p>
 */

public final class TraceIdResolver {

    private static final int MAX_TRACE_ID_LENGTH = 128;

    private TraceIdResolver() {
    }

    public static String resolve(String incomingTraceId) {
        if (incomingTraceId == null || incomingTraceId.isBlank()) {
            return TraceIdGenerator.generate();
        }
        String traceId = incomingTraceId.trim();
        if (!isValid(traceId)) {
            return TraceIdGenerator.generate();
        }
        return traceId;
    }

    public static boolean isValid(String traceId) {
        if (traceId == null || traceId.isBlank() || traceId.length() > MAX_TRACE_ID_LENGTH) {
            return false;
        }
        for (int i = 0; i < traceId.length(); i++) {
            char value = traceId.charAt(i);
            if (!isAllowed(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAllowed(char value) {
        return value >= 'a' && value <= 'z'
                || value >= 'A' && value <= 'Z'
                || value >= '0' && value <= '9'
                || value == '-'
                || value == '_'
                || value == '.';
    }
}
