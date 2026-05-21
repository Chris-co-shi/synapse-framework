package com.indigo.synapse.audit.context;

import com.indigo.synapse.audit.event.AuditSubject;

import java.util.Optional;

public final class AuditContext {

    private static final ThreadLocal<AuditContextSnapshot> CURRENT = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void set(AuditContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        CURRENT.set(snapshot);
    }

    public static AuditContextScope scope(AuditContextSnapshot snapshot) {
        AuditContextSnapshot previous = CURRENT.get();
        set(snapshot);
        return new AuditContextScope(previous);
    }

    public static Optional<AuditContextSnapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Optional<AuditSubject> currentSubject() {
        return current().map(AuditContextSnapshot::subject);
    }

    public static Optional<String> currentTraceId() {
        return current().map(AuditContextSnapshot::traceId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
