package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

import java.util.List;

public final class CompositeAuditLogPort implements AuditLogPort {

    private final List<AuditLogPort> delegates;

    public CompositeAuditLogPort(List<AuditLogPort> delegates) {
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("delegates must not be empty");
        }
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        for (AuditLogPort delegate : delegates) {
            delegate.record(event);
        }
    }
}
