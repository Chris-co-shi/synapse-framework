package com.indigo.synapse.datapermission.context;

import com.indigo.synapse.datapermission.model.DataPermissionPolicy;

public record DataPermissionContextSnapshot(String subjectId, String tenantId, DataPermissionPolicy policy) {

    public DataPermissionContextSnapshot {
        if (subjectId == null || subjectId.isBlank()) {
            throw new IllegalArgumentException("subjectId must not be blank");
        }
        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }
    }
}
