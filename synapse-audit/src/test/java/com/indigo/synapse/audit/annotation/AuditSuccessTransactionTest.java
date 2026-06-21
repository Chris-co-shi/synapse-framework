package com.indigo.synapse.audit.annotation;

import com.indigo.synapse.audit.autoconfigure.SynapseAuditAutoConfiguration;
import com.indigo.synapse.audit.publish.AuditSuccessPolicy;
import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.core.MessageEnvelope;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.messaging.reliability.OutboxStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuditSuccessTransactionTest {
    @Test
    void successOutboxShouldCommitAndRollbackWithBusinessTransaction() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class,
                        SynapseAuditAutoConfiguration.class))
                .withUserConfiguration(TestConfiguration.class)
                .run(context -> {
                    AuditedService service = context.getBean(AuditedService.class);
                    TransactionalOutboxStore outbox = context.getBean(TransactionalOutboxStore.class);

                    assertThat(service.commit()).isEqualTo("committed");
                    assertThat(outbox.committed()).hasSize(1);

                    assertThat(service.rollbackOnly()).isEqualTo("rolled-back");
                    assertThat(outbox.committed()).hasSize(1);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    static class TestConfiguration {
        @Bean
        TestTransactionManager transactionManager() { return new TestTransactionManager(); }

        @Bean
        TransactionalOutboxStore outboxStore() { return new TransactionalOutboxStore(); }

        @Bean
        ReliableMessagePublisher reliableMessagePublisher(TransactionalOutboxStore outbox) {
            return new ReliableMessagePublisher(outbox, new OperationContextMessagePropagator());
        }

        @Bean
        OperationContextProvider operationContextProvider() {
            OperationActor actor = new OperationActor(OperationActorType.USER,
                    "user-1", "User", "tenant-1", Map.of());
            OperationContext context = new OperationContext(actor, actor, null,
                    "trace-1", "tenant-1", "request-1", Instant.now(), Map.of());
            return () -> Optional.of(context);
        }

        @Bean
        AuditedService auditedService() { return new AuditedService(); }
    }

    static class AuditedService {
        @Transactional
        @Audited(action = "order.commit", targetType = "ORDER",
                successPolicy = AuditSuccessPolicy.TRANSACTIONAL_OUTBOX)
        public String commit() { return "committed"; }

        @Transactional
        @Audited(action = "order.rollback", targetType = "ORDER",
                successPolicy = AuditSuccessPolicy.TRANSACTIONAL_OUTBOX)
        public String rollbackOnly() {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "rolled-back";
        }
    }

    static final class TransactionalOutboxStore implements OutboxStore {
        private final List<MessageEnvelope> committed = new ArrayList<>();

        @Override
        public void append(MessageEnvelope envelope) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    committed.add(envelope);
                }
            });
        }

        List<MessageEnvelope> committed() { return List.copyOf(committed); }
    }

    static final class TestTransactionManager extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
