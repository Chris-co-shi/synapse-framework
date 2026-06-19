package com.indigo.synapse.observability;

import io.micrometer.observation.Observation;

/**
 * Synapse Observation 上下文，只允许稳定、低基数的 module 与 operation。
 */
public final class SynapseObservationContext extends Observation.Context {

    private final String module;
    private final String operation;

    public SynapseObservationContext(String module, String operation) {
        this.module = requireTag("module", module);
        this.operation = requireTag("operation", operation);
    }

    public String module() {
        return module;
    }

    public String operation() {
        return operation;
    }

    private static String requireTag(String name, String value) {
        if (value == null || !value.matches("[a-z0-9._-]{1,64}")) {
            throw new IllegalArgumentException(name + " must be a stable low-cardinality tag");
        }
        return value;
    }
}
