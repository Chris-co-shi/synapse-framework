package com.indigo.synapse.audit.context;

public final class AuditContextScope implements AutoCloseable {

    private final AuditContextSnapshot previous;
    private boolean closed;

    AuditContextScope(AuditContextSnapshot previous) {
        this.previous = previous;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        AuditContext.set(previous);
    }
}
