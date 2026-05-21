package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

public final class NoopAuditLogPort implements AuditLogPort {

    @Override
    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
    }
}
