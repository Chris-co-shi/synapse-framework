package com.indigo.synapse.audit.publish;

public enum AuditFailurePolicy {
    NONE,
    BEST_EFFORT_AFTER_ROLLBACK,
    REQUIRES_NEW_AFTER_ROLLBACK,
    EXTERNAL_SINK
}
