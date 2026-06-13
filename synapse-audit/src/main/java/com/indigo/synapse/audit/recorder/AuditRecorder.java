package com.indigo.synapse.audit.recorder;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;

public final class AuditRecorder {

    private final AuditLogPort auditLogPort;
    private final AuditEventContextEnricher contextEnricher;

    public AuditRecorder(AuditLogPort auditLogPort) {
        this(auditLogPort, new AuditEventContextEnricher(new DefaultOperationContextProvider()));
    }

    public AuditRecorder(AuditLogPort auditLogPort, AuditEventContextEnricher contextEnricher) {
        if (auditLogPort == null) {
            throw new IllegalArgumentException("auditLogPort must not be null");
        }
        if (contextEnricher == null) {
            throw new IllegalArgumentException("contextEnricher must not be null");
        }
        this.auditLogPort = auditLogPort;
        this.contextEnricher = contextEnricher;
    }

    public void record(AuditEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }
        AuditEvent enriched = contextEnricher.enrich(event);
        enriched.requireRecordable();
        auditLogPort.record(enriched);
    }
}
