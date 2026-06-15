package com.indigo.synapse.webflux.trace;

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
