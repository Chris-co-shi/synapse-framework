package com.indigo.synapse.web.trace;

import java.util.UUID;

public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
