package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;

public interface AuditPublisher {
    void publishSuccess(AuditEvent event, AuditSuccessPolicy policy);

    void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable businessFailure);
}
