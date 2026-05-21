package com.indigo.synapse.audit.port;

import com.indigo.synapse.audit.event.AuditEvent;

public interface AuditLogPort {

    void record(AuditEvent event);
}
