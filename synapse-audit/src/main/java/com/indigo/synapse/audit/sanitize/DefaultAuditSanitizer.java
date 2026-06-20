package com.indigo.synapse.audit.sanitize;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.SensitiveAuditValueMasker;

import java.util.Objects;

/** 使用 AuditEvent 构造约束和敏感 key 规则执行最终脱敏。 */
public final class DefaultAuditSanitizer implements AuditSanitizer {
    @Override
    public AuditEvent sanitize(AuditEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        return new AuditEvent(event.action(), event.subject(), event.target(), event.occurredAt(), event.outcome(),
                event.traceId(), event.message(), SensitiveAuditValueMasker.mask(event.attributes()));
    }
}
