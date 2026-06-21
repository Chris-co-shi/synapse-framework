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
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 将审计投递委托给 Messaging 发布器的默认实现。 */
public final class MessagingAuditPublisher implements AuditPublisher {
    private static final Logger LOGGER = Logger.getLogger(MessagingAuditPublisher.class.getName());

    private final BestEffortMessagePublisher bestEffortPublisher;
    private final ReliableMessagePublisher reliablePublisher;
    private final AuditEventContextEnricher enricher;
    private final AuditSanitizer sanitizer;
    private final TransactionOperations requiresNewOperations;
    private final AuditFailureSink failureSink;
    private final String destination;

    public MessagingAuditPublisher(BestEffortMessagePublisher bestEffortPublisher,
                                   ReliableMessagePublisher reliablePublisher,
                                   AuditEventContextEnricher enricher,
                                   AuditSanitizer sanitizer,
                                   TransactionOperations requiresNewOperations,
                                   AuditFailureSink failureSink,
                                   String destination) {
        this.bestEffortPublisher = bestEffortPublisher;
        this.reliablePublisher = reliablePublisher;
        this.enricher = Objects.requireNonNull(enricher, "enricher must not be null");
        this.sanitizer = Objects.requireNonNull(sanitizer, "sanitizer must not be null");
        this.requiresNewOperations = requiresNewOperations;
        this.failureSink = failureSink;
        this.destination = Objects.requireNonNull(destination, "destination must not be null");
    }

    @Override
    public void publishSuccess(AuditEvent event, AuditSuccessPolicy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        MessageEnvelope envelope = envelope(prepare(event));
        if (policy == AuditSuccessPolicy.TRANSACTIONAL_OUTBOX) {
            requireReliablePublisher().publish(envelope);
            return;
        }
        try {
            sendBestEffort(envelope);
        } catch (RuntimeException failure) {
            LOGGER.log(Level.WARNING, "Best-effort success audit publishing failed", failure);
        }
    }

    @Override
    public void publishFailure(AuditEvent event, AuditFailurePolicy policy, Throwable businessFailure) {
        Objects.requireNonNull(policy, "policy must not be null");
        Objects.requireNonNull(businessFailure, "businessFailure must not be null");
        if (policy == AuditFailurePolicy.NONE) return;

        try {
            AuditEvent prepared = prepare(event);
            MessageEnvelope envelope = envelope(prepared);
            Runnable action = switch (policy) {
                case NONE -> () -> { };
                case BEST_EFFORT_AFTER_ROLLBACK -> () -> sendBestEffort(envelope);
                case REQUIRES_NEW_AFTER_ROLLBACK -> () -> publishRequiresNew(envelope);
                case EXTERNAL_SINK -> () -> requireFailureSink().publish(prepared);
            };
            executeAfterRollback(action, businessFailure);
        } catch (RuntimeException auditFailure) {
            attachFailure(businessFailure, auditFailure);
        }
    }

    private AuditEvent prepare(AuditEvent event) {
        AuditEvent prepared = sanitizer.sanitize(enricher.enrich(event));
        prepared.requireRecordable();
        return prepared;
    }

    private void executeAfterRollback(Runnable action, Throwable businessFailure) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) runFailureAction(action, businessFailure);
                }
            });
            return;
        }
        runFailureAction(action, businessFailure);
    }

    private void runFailureAction(Runnable action, Throwable businessFailure) {
        try {
            action.run();
        } catch (RuntimeException auditFailure) {
            attachFailure(businessFailure, auditFailure);
            LOGGER.log(Level.WARNING, "Failure audit publishing failed", auditFailure);
        }
    }

    private void publishRequiresNew(MessageEnvelope envelope) {
        if (requiresNewOperations == null) {
            throw new IllegalStateException("REQUIRES_NEW failure audit requires a PlatformTransactionManager");
        }
        requiresNewOperations.executeWithoutResult(status -> requireReliablePublisher().publish(envelope));
    }

    private void sendBestEffort(MessageEnvelope envelope) {
        if (bestEffortPublisher == null) {
            throw new IllegalStateException("Best-effort audit publisher is unavailable");
        }
        MessagePublishResult result = bestEffortPublisher.publish(envelope);
        if (!result.isAccepted()) {
            throw new IllegalStateException("Audit message was rejected: " + result.reason());
        }
    }

    private ReliableMessagePublisher requireReliablePublisher() {
        if (reliablePublisher == null) {
            throw new IllegalStateException("Transactional audit requires ReliableMessagePublisher");
        }
        return reliablePublisher;
    }

    private AuditFailureSink requireFailureSink() {
        if (failureSink == null) {
            throw new IllegalStateException("EXTERNAL_SINK failure audit requires AuditFailureSink");
        }
        return failureSink;
    }

    private void attachFailure(Throwable businessFailure, RuntimeException auditFailure) {
        if (businessFailure != auditFailure) businessFailure.addSuppressed(auditFailure);
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
