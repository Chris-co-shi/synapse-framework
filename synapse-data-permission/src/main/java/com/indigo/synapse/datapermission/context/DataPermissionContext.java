package com.indigo.synapse.datapermission.context;

import java.util.Optional;

public final class DataPermissionContext {

    private static final ThreadLocal<DataPermissionContextSnapshot> CURRENT = new ThreadLocal<>();

    private DataPermissionContext() {
    }

    public static void set(DataPermissionContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        CURRENT.set(snapshot);
    }

    public static DataPermissionContextScope scope(DataPermissionContextSnapshot snapshot) {
        DataPermissionContextSnapshot previous = CURRENT.get();
        set(snapshot);
        return new DataPermissionContextScope(previous);
    }

    public static Optional<DataPermissionContextSnapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static void clear() {
        CURRENT.remove();
    }
}
