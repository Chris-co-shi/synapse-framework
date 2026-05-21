package com.indigo.synapse.audit.event;

public record AuditSubject(String subjectType, String subjectId, String tenantId) {

    public AuditSubject {
        if (subjectType == null || subjectType.isBlank()) {
            throw new IllegalArgumentException("subjectType must not be blank");
        }
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("subjectId must not be blank");
        }
    }
}
