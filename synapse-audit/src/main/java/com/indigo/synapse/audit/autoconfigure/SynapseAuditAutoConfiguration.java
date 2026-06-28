package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.annotation.AuditAspect;
import com.indigo.synapse.audit.annotation.AuditMethodAdvisor;
import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.publish.AuditFailureSink;
import com.indigo.synapse.audit.publish.AuditPublisher;
import com.indigo.synapse.audit.publish.MessagingAuditPublisher;
import com.indigo.synapse.audit.sanitize.AuditSanitizer;
import com.indigo.synapse.audit.sanitize.DefaultAuditSanitizer;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

/** Audit 模块自动配置。 */
@AutoConfiguration(after = TransactionAutoConfiguration.class)
@EnableConfigurationProperties(SynapseAuditProperties.class)
@ConditionalOnProperty(prefix = "synapse.audit", name = "enabled", matchIfMissing = true)
public class SynapseAuditAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    public static AuditTransactionAdvisorOrderPostProcessor synapseAuditTransactionAdvisorOrderPostProcessor() {
        return new AuditTransactionAdvisorOrderPostProcessor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(ReliableMessagePublisher.class)
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    public AuditTransactionInfrastructureValidator synapseAuditTransactionInfrastructureValidator(
            ConfigurableListableBeanFactory beanFactory) {
        return new AuditTransactionInfrastructureValidator(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditSanitizer synapseAuditSanitizer() {
        return new DefaultAuditSanitizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditPublisher synapseAuditPublisher(
            ObjectProvider<BestEffortMessagePublisher> bestEffortPublisher,
            ObjectProvider<ReliableMessagePublisher> reliablePublisher,
            ObjectProvider<PlatformTransactionManager> transactionManagers,
            ObjectProvider<AuditFailureSink> failureSinks,
            AuditEventContextEnricher contextEnricher,
            AuditSanitizer sanitizer,
            SynapseAuditProperties properties) {
        TransactionOperations requiresNew = requiresNewOperations(transactionManagers.getIfUnique());
        return new MessagingAuditPublisher(bestEffortPublisher.getIfUnique(), reliablePublisher.getIfUnique(),
                contextEnricher, sanitizer, requiresNew, failureSinks.getIfUnique(), properties.getDestination());
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditAspect synapseAuditAspect(AuditPublisher publisher) {
        return new AuditAspect(publisher);
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditMethodAdvisor synapseAuditMethodAdvisor(AuditAspect aspect) {
        return new AuditMethodAdvisor(aspect);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditEventContextEnricher synapseAuditEventContextEnricher(
            ObjectProvider<OperationContextProvider> operationContextProvider) {
        OperationContextProvider provider = operationContextProvider.getIfAvailable(DefaultOperationContextProvider::new);
        return new AuditEventContextEnricher(provider);
    }

    private TransactionOperations requiresNewOperations(PlatformTransactionManager transactionManager) {
        if (transactionManager == null) return null;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }
}
