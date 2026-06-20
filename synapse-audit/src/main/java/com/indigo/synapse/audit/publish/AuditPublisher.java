package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;

/** 审计事件发布入口。 */
@FunctionalInterface
public interface AuditPublisher {
    void publish(AuditEvent event, AuditFailurePolicy failurePolicy);
}
