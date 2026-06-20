package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.sanitize.AuditSanitizer;
import com.indigo.synapse.messaging.core.MessageDestination;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.core.MessageMetadata;
import com.indigo.synapse.messaging.core.MessagePublishResult;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;

import java.util.Objects;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 将审计投递委托给 Messaging 发布器的唯一默认实现。
 *
 * <p>普通审计使用 best-effort 并在失败时告警；关键审计使用 reliable，异常直接传播。
 * 本实现不直接访问 StreamBridge、Broker 或 OutboxStore。</p>
 */
public final class MessagingAuditPublisher implements AuditPublisher {
    private static final Logger LOGGER = Logger.getLogger(MessagingAuditPublisher.class.getName());
    private final BestEffortMessagePublisher bestEffortPublisher;
    private final ReliableMessagePublisher reliablePublisher;
    private final AuditEventContextEnricher enricher;
    private final AuditSanitizer sanitizer;
    private final String destination;

    public MessagingAuditPublisher(BestEffortMessagePublisher bestEffortPublisher,
                                   ReliableMessagePublisher reliablePublisher,
                                   AuditEventContextEnricher enricher, AuditSanitizer sanitizer,
                                   String destination) {
        this.bestEffortPublisher = bestEffortPublisher;
        this.reliablePublisher = reliablePublisher;
        this.enricher = Objects.requireNonNull(enricher, "enricher must not be null");
        this.sanitizer = Objects.requireNonNull(sanitizer, "sanitizer must not be null");
        this.destination = Objects.requireNonNull(destination, "destination must not be null");
    }

    @Override
    public void publish(AuditEvent event, AuditFailurePolicy failurePolicy) {
        Objects.requireNonNull(failurePolicy, "failurePolicy must not be null");
        AuditEvent prepared = sanitizer.sanitize(enricher.enrich(event));
        prepared.requireRecordable();
        MessageEnvelope envelope = envelope(prepared);
        if (failurePolicy == AuditFailurePolicy.ROLLBACK) {
            if (reliablePublisher == null) throw new IllegalStateException("Reliable audit requires ReliableMessagePublisher");
            reliablePublisher.publish(envelope);
            return;
        }
        try {
            if (bestEffortPublisher == null) throw new IllegalStateException("Best-effort audit publisher is unavailable");
            MessagePublishResult result = bestEffortPublisher.publish(envelope);
            if (!result.isAccepted()) throw new IllegalStateException("Audit message was rejected: " + result.reason());
        } catch (RuntimeException failure) {
            LOGGER.log(Level.WARNING, "Audit publishing failed; business execution continues", failure);
        }
    }

    private MessageEnvelope envelope(AuditEvent event) {
        MessageMetadata created = MessageMetadata.create(event.eventId(), "synapse.audit.event",
                event.sourceService() == null ? "unknown-service" : event.sourceService());
        MessageMetadata metadata = new MessageMetadata(created.messageId(), created.eventId(), created.messageType(),
                created.sourceService(), "text/plain", created.schemaVersion(), Map.of(),
                event.occurredAt(), created.createdAt());
        String payload = event.action() + "|" + event.outcome() + "|" + event.target().targetType()
                + "|" + event.target().targetId() + "|" + event.attributes();
        return new MessageEnvelope(metadata, MessageDestination.of(destination), payload);
    }
}
