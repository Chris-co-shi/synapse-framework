package com.indigo.synapse.tenant.context;

import java.util.Optional;

public final class TenantContext {

    private static final ThreadLocal<TenantContextSnapshot> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        CURRENT.set(snapshot);
    }

    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            clear();
            return;
        }
        set(new TenantContextSnapshot(tenantId));
    }

    public static TenantContextScope scope(TenantContextSnapshot snapshot) {
        TenantContextSnapshot previous = CURRENT.get();
        set(snapshot);
        return new TenantContextScope(previous);
    }

    public static Optional<TenantContextSnapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Optional<String> currentTenantId() {
        return current().map(TenantContextSnapshot::tenantId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
