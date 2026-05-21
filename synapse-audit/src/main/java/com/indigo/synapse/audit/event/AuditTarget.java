package com.indigo.synapse.audit.event;

public record AuditTarget(String targetType, String targetId) {

    public AuditTarget {
        if (targetType == null || targetType.isBlank()) {
            throw new IllegalArgumentException("targetType must not be blank");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("targetId must not be blank");
        }
    }
}
