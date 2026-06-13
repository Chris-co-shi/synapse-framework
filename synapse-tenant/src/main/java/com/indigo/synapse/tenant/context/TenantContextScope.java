package com.indigo.synapse.tenant.context;

public final class TenantContextScope implements AutoCloseable {

    private final TenantContextSnapshot previous;
    private boolean closed;

    TenantContextScope(TenantContextSnapshot previous) {
        this.previous = previous;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        TenantContext.set(previous);
    }
}
