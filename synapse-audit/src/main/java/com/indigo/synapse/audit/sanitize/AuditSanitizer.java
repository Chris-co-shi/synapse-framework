package com.indigo.synapse.audit.sanitize;

import com.indigo.synapse.audit.event.AuditEvent;

/** 审计事件发送前的最终脱敏端口。 */
@FunctionalInterface
public interface AuditSanitizer {
    AuditEvent sanitize(AuditEvent event);
}
