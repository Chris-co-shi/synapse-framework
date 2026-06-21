package com.indigo.synapse.audit.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class AuditTransactionOrderTest {
    @Test
    void shouldNormalizeDefaultTransactionAdvisorOrder() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AopAutoConfiguration.class,
                        SynapseAuditAutoConfiguration.class))
                .withUserConfiguration(TransactionConfiguration.class)
                .run(context -> {
                    Advisor advisor = context.getBean(
                            TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME, Advisor.class);
                    assertThat(((org.springframework.core.Ordered) advisor).getOrder()).isZero();
                });
    }

    @Test
    void validatorShouldRejectMissingOrIncorrectTransactionAdvisor() {
        DefaultListableBeanFactory missing = new DefaultListableBeanFactory();
        assertThatIllegalStateException()
                .isThrownBy(() -> new AuditTransactionInfrastructureValidator(missing)
                        .afterSingletonsInstantiated())
                .withMessageContaining("transaction management");

        DefaultListableBeanFactory incorrect = new DefaultListableBeanFactory();
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();
        advisor.setOrder(500);
        incorrect.registerSingleton(TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME, advisor);
        assertThatIllegalStateException()
                .isThrownBy(() -> new AuditTransactionInfrastructureValidator(incorrect)
                        .afterSingletonsInstantiated())
                .withMessageContaining("order 0");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    static class TransactionConfiguration {
        @Bean
        TestTransactionManager transactionManager() {
            return new TestTransactionManager();
        }
    }

    static final class TestTransactionManager extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { }
        @Override protected void doRollback(DefaultTransactionStatus status) { }
    }
}
