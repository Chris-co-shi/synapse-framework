package com.indigo.synapse.audit.recorder;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.port.AuditLogPort;

public final class AuditRecorder {

    private final AuditLogPort auditLogPort;

    public AuditRecorder(AuditLogPort auditLogPort) {
        if (auditLogPort == null) {
            throw new IllegalArgumentException("auditLogPort must not be null");
        }
        this.auditLogPort = auditLogPort;
    }

    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        auditLogPort.record(event);
    }
}
