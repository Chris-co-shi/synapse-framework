package com.indigo.synapse.audit.context;

import com.indigo.synapse.audit.event.AuditSubject;

public record AuditContextSnapshot(AuditSubject subject, String traceId) {

    public AuditContextSnapshot {
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
    }
}
