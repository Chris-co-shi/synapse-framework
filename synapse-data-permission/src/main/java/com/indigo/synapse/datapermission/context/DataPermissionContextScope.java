package com.indigo.synapse.datapermission.context;

public final class DataPermissionContextScope implements AutoCloseable {

    private final DataPermissionContextSnapshot previous;
    private boolean closed;

    DataPermissionContextScope(DataPermissionContextSnapshot previous) {
        this.previous = previous;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        DataPermissionContext.set(previous);
    }
}
