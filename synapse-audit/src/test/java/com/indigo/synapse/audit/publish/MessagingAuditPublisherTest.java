package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.AuditEvent;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.event.AuditOutcome;
import com.indigo.synapse.audit.event.AuditTarget;
import com.indigo.synapse.audit.sanitize.DefaultAuditSanitizer;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationSource;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.messaging.transport.MessageTransportResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingAuditPublisherTest {
    @AfterEach
    void clearTransaction() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void bestEffortSuccessShouldNotFailBusiness() {
        BestEffortMessagePublisher bestEffort = new BestEffortMessagePublisher(envelope -> {
            throw new IllegalStateException("broker unavailable");
        }, new OperationContextMessagePropagator());

        publisher(bestEffort, null, null, null)
                .publishSuccess(event(AuditOutcome.SUCCESS), AuditSuccessPolicy.BEST_EFFORT);
    }

    @Test
    void transactionalSuccessShouldAppendInCurrentTransaction() {
        AtomicReference<MessageEnvelope> stored = new AtomicReference<>();
        ReliableMessagePublisher reliable = reliable(stored);
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher(null, reliable, null, null)
                .publishSuccess(event(AuditOutcome.SUCCESS), AuditSuccessPolicy.TRANSACTIONAL_OUTBOX);

        assertThat(stored.get().metadata().eventId()).isNotBlank();
        assertThat(stored.get().payload()).contains("password=******");
    }

    @Test
    void transactionalSuccessShouldFailWithoutReliablePublisher() {
        assertThatThrownBy(() -> publisher(null, null, null, null)
                .publishSuccess(event(AuditOutcome.SUCCESS), AuditSuccessPolicy.TRANSACTIONAL_OUTBOX))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ReliableMessagePublisher");
    }

    @Test
    void noneShouldSkipFailureAudit() {
        AtomicInteger sent = new AtomicInteger();
        BestEffortMessagePublisher bestEffort = acceptedPublisher(sent);
        Throwable businessFailure = new IllegalStateException("business");

        publisher(bestEffort, null, null, null)
                .publishFailure(event(AuditOutcome.FAILURE), AuditFailurePolicy.NONE, businessFailure);

        assertThat(sent).hasValue(0);
        assertThat(businessFailure.getSuppressed()).isEmpty();
    }

    @Test
    void bestEffortFailureShouldRunOnlyAfterRollback() {
        AtomicInteger sent = new AtomicInteger();
        beginSynchronizedTransaction();
        Throwable businessFailure = new IllegalStateException("business");

        publisher(acceptedPublisher(sent), null, null, null)
                .publishFailure(event(AuditOutcome.FAILURE),
                        AuditFailurePolicy.BEST_EFFORT_AFTER_ROLLBACK, businessFailure);

        assertThat(sent).hasValue(0);
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(sent).hasValue(1);
    }

    @Test
    void failureAuditShouldNotRunWhenTransactionCommits() {
        AtomicInteger sent = new AtomicInteger();
        beginSynchronizedTransaction();

        publisher(acceptedPublisher(sent), null, null, null)
                .publishFailure(event(AuditOutcome.FAILURE),
                        AuditFailurePolicy.BEST_EFFORT_AFTER_ROLLBACK,
                        new IllegalStateException("business"));

        complete(TransactionSynchronization.STATUS_COMMITTED);
        assertThat(sent).hasValue(0);
    }

    @Test
    void requiresNewFailureShouldAppendOutboxInIndependentTransaction() {
        AtomicReference<MessageEnvelope> stored = new AtomicReference<>();
        AtomicInteger newTransactions = new AtomicInteger();
        TransactionOperations requiresNew = action -> {
            newTransactions.incrementAndGet();
            TransactionSynchronizationManager.setActualTransactionActive(true);
            try {
                return action.doInTransaction(new SimpleTransactionStatus());
            } finally {
                TransactionSynchronizationManager.setActualTransactionActive(false);
            }
        };
        beginSynchronizedTransaction();

        publisher(null, reliable(stored), requiresNew, null)
                .publishFailure(event(AuditOutcome.FAILURE),
                        AuditFailurePolicy.REQUIRES_NEW_AFTER_ROLLBACK,
                        new IllegalStateException("business"));
        complete(TransactionSynchronization.STATUS_ROLLED_BACK);

        assertThat(newTransactions).hasValue(1);
        assertThat(stored.get()).isNotNull();
    }

    @Test
    void externalSinkShouldRunAfterRollback() {
        AtomicReference<AuditEvent> received = new AtomicReference<>();
        beginSynchronizedTransaction();

        publisher(null, null, null, received::set)
                .publishFailure(event(AuditOutcome.FAILURE), AuditFailurePolicy.EXTERNAL_SINK,
                        new IllegalStateException("business"));
        assertThat(received).hasValue(null);

        complete(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(received.get().outcome()).isEqualTo(AuditOutcome.FAILURE);
    }

    @Test
    void failureAuditErrorShouldBeSuppressedOnBusinessFailure() {
        Throwable businessFailure = new IllegalStateException("business");
        AuditFailureSink sink = event -> { throw new IllegalArgumentException("sink failed"); };

        publisher(null, null, null, sink)
                .publishFailure(event(AuditOutcome.FAILURE), AuditFailurePolicy.EXTERNAL_SINK, businessFailure);

        assertThat(businessFailure.getSuppressed())
                .singleElement().isInstanceOf(IllegalArgumentException.class);
    }

    private static BestEffortMessagePublisher acceptedPublisher(AtomicInteger sent) {
        return new BestEffortMessagePublisher(envelope -> {
            sent.incrementAndGet();
            return MessageTransportResult.accepted("transport-1");
        }, new OperationContextMessagePropagator());
    }

    private static ReliableMessagePublisher reliable(AtomicReference<MessageEnvelope> stored) {
        return new ReliableMessagePublisher(stored::set, new OperationContextMessagePropagator());
    }

    private static MessagingAuditPublisher publisher(
            BestEffortMessagePublisher bestEffort,
            ReliableMessagePublisher reliable,
            TransactionOperations requiresNew,
            AuditFailureSink sink) {
        OperationActor actor = new OperationActor(OperationActorType.USER, "user-1", "User", "tenant-1", Map.of());
        OperationSource source = new OperationSource("HTTP", "order-service", "instance-1", "/orders", Map.of());
        OperationContext context = new OperationContext(actor, actor, source, "trace-1", "tenant-1", "request-1",
                Instant.parse("2026-06-20T00:00:00Z"), Map.of());
        return new MessagingAuditPublisher(bestEffort, reliable,
                new AuditEventContextEnricher(() -> Optional.of(context)), new DefaultAuditSanitizer(),
                requiresNew, sink, "audit-out-0");
    }

    private static AuditEvent event(AuditOutcome outcome) {
        return new AuditEvent("order.create", null, new AuditTarget("ORDER", "1"), Instant.now(),
                outcome, null, null, Map.of("password", "secret"));
    }

    private static void beginSynchronizedTransaction() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private static void complete(int status) {
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCompletion(status);
        }
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }
}
