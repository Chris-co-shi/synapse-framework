package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;

/** 应用提供的外部失败审计输出端口。 */
@FunctionalInterface
public interface AuditFailureSink {
    void publish(AuditEvent event);
}
