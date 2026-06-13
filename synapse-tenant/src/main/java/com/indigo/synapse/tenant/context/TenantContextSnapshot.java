package com.indigo.synapse.tenant.context;

public record TenantContextSnapshot(String tenantId) {

    public TenantContextSnapshot {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
    }
}
