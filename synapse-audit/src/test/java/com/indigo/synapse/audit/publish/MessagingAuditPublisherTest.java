package com.indigo.synapse.audit.publish;

import com.indigo.synapse.audit.event.*;
import com.indigo.synapse.audit.sanitize.DefaultAuditSanitizer;
import com.indigo.synapse.core.context.*;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.messaging.transport.MessageTransportResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessagingAuditPublisherTest {
    @AfterEach void clearTransaction() { TransactionSynchronizationManager.setActualTransactionActive(false); }

    @Test
    void ordinaryAuditShouldContinueWhenTransportFails() {
        BestEffortMessagePublisher bestEffort = new BestEffortMessagePublisher(envelope -> {
            throw new IllegalStateException("broker unavailable");
        }, new OperationContextMessagePropagator());
        MessagingAuditPublisher publisher = publisher(bestEffort, null);
        publisher.publish(event(), AuditFailurePolicy.CONTINUE);
    }

    @Test
    void criticalAuditShouldRegisterSanitizedEnvelopeInCurrentTransaction() {
        AtomicReference<MessageEnvelope> stored = new AtomicReference<>();
        ReliableMessagePublisher reliable = new ReliableMessagePublisher(stored::set,
                new OperationContextMessagePropagator());
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher(null, reliable).publish(event(), AuditFailurePolicy.ROLLBACK);

        assertThat(stored.get().metadata().eventId()).isNotBlank();
        assertThat(stored.get().payload()).contains("password=******");
    }

    @Test
    void criticalAuditShouldFailWithoutReliablePublisher() {
        assertThatThrownBy(() -> publisher(null, null).publish(event(), AuditFailurePolicy.ROLLBACK))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Reliable audit");
    }

    private static MessagingAuditPublisher publisher(BestEffortMessagePublisher bestEffort,
                                                       ReliableMessagePublisher reliable) {
        OperationActor actor = new OperationActor(OperationActorType.USER, "user-1", "User", "tenant-1", Map.of());
        OperationSource source = new OperationSource("HTTP", "order-service", "instance-1", "/orders", Map.of());
        OperationContext context = new OperationContext(actor, actor, source, "trace-1", "tenant-1", "request-1",
                Instant.parse("2026-06-20T00:00:00Z"), Map.of());
        return new MessagingAuditPublisher(bestEffort, reliable,
                new AuditEventContextEnricher(() -> Optional.of(context)), new DefaultAuditSanitizer(), "audit-out-0");
    }

    private static AuditEvent event() {
        return new AuditEvent("order.create", null, new AuditTarget("ORDER", "1"), Instant.now(),
                AuditOutcome.SUCCESS, null, null, Map.of("password", "secret"));
    }
}
