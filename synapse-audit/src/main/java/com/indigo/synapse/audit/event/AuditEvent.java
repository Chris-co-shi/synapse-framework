package com.indigo.synapse.audit.event;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
        String action,
        AuditSubject subject,
        AuditTarget target,
        Instant occurredAt,
        AuditOutcome outcome,
        String traceId,
        String message,
        Map<String, String> attributes
) {

    public AuditEvent {
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("occurredAt must not be null");
        }
        if (outcome == null) {
            throw new IllegalArgumentException("outcome must not be null");
        }
        attributes = SensitiveAuditValueMasker.mask(attributes);
    }

    public void requireRecordable() {
        if (subject == null) {
            throw new IllegalArgumentException("subject must not be null");
        }
        if (traceId == null || traceId.isBlank()) {
            throw new IllegalArgumentException("traceId must not be blank");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String action;
        private AuditSubject subject;
        private AuditTarget target;
        private Instant occurredAt;
        private AuditOutcome outcome;
        private String traceId;
        private String message;
        private Map<String, String> attributes;

        private Builder() {
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder subject(AuditSubject subject) {
            this.subject = subject;
            return this;
        }

        public Builder target(AuditTarget target) {
            this.target = target;
            return this;
        }

        public Builder occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return this;
        }

        public Builder outcome(AuditOutcome outcome) {
            this.outcome = outcome;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(action, subject, target, occurredAt, outcome, traceId, message, attributes);
        }
    }
}
