package com.indigo.synapse.audit.autoconfigure;

import com.indigo.synapse.audit.event.AuditEventContextEnricher;
import com.indigo.synapse.audit.port.AuditLogPort;
import com.indigo.synapse.audit.port.CompositeAuditLogPort;
import com.indigo.synapse.audit.port.NoopAuditLogPort;
import com.indigo.synapse.audit.recorder.AuditRecorder;
import com.indigo.synapse.audit.annotation.AuditAspect;
import com.indigo.synapse.audit.annotation.AuditMethodAdvisor;
import com.indigo.synapse.audit.publish.AuditPublisher;
import com.indigo.synapse.audit.publish.MessagingAuditPublisher;
import com.indigo.synapse.audit.sanitize.AuditSanitizer;
import com.indigo.synapse.audit.sanitize.DefaultAuditSanitizer;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import java.util.List;

/**
 * Audit 模块自动配置。
 *
 * <p>该配置只提供审计事件记录入口、上下文补齐器和默认 Noop 端口。它不依赖 synapse-data，
 * 不创建审计表，不提供 Repository，不实现审计查询后台，也不绑定任何消息队列或数据库。</p>
 *
 * <p>消费方如需落库、发消息或写日志，应提供自己的 {@link AuditLogPort} Bean。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseAuditProperties.class)
@ConditionalOnProperty(prefix = "synapse.audit", name = "enabled", matchIfMissing = true)
public class SynapseAuditAutoConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnMissingBean(DefaultAdvisorAutoProxyCreator.class)
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    public static DefaultAdvisorAutoProxyCreator synapseAuditAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditSanitizer synapseAuditSanitizer() { return new DefaultAuditSanitizer(); }

    @Bean
    @ConditionalOnMissingBean
    public AuditPublisher synapseAuditPublisher(
            ObjectProvider<BestEffortMessagePublisher> bestEffortPublisher,
            ObjectProvider<ReliableMessagePublisher> reliablePublisher,
            AuditEventContextEnricher contextEnricher,
            AuditSanitizer sanitizer,
            SynapseAuditProperties properties) {
        return new MessagingAuditPublisher(bestEffortPublisher.getIfUnique(), reliablePublisher.getIfUnique(),
                contextEnricher, sanitizer, properties.getDestination());
    }

    @Bean
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditAspect synapseAuditAspect(AuditPublisher publisher) { return new AuditAspect(publisher); }

    @Bean
    @ConditionalOnProperty(prefix = "synapse.audit", name = "aop-enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public AuditMethodAdvisor synapseAuditMethodAdvisor(AuditAspect aspect) { return new AuditMethodAdvisor(aspect); }

    /**
     * 默认 Noop 审计端口。
     *
     * <p>没有消费方实现时，审计事件不会被持久化或外发，避免 framework 强制绑定存储设施。</p>
     */
    @Bean
    @ConditionalOnMissingBean(AuditLogPort.class)
    public AuditLogPort synapseNoopAuditLogPort() {
        return new NoopAuditLogPort();
    }

    /**
     * 创建审计事件上下文补齐器。
     *
     * <p>优先复用容器中的 OperationContextProvider；没有时使用 core 默认实现。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditEventContextEnricher synapseAuditEventContextEnricher(
            ObjectProvider<OperationContextProvider> operationContextProvider
    ) {
        OperationContextProvider provider = operationContextProvider.getIfAvailable(DefaultOperationContextProvider::new);
        return new AuditEventContextEnricher(provider);
    }

    /**
     * 创建审计记录器。
     *
     * <p>如果存在多个 AuditLogPort，会组合后依次调用；如果只有一个，则直接使用该端口。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditRecorder synapseAuditRecorder(
            ObjectProvider<AuditLogPort> auditLogPorts,
            AuditEventContextEnricher contextEnricher
    ) {
        List<AuditLogPort> delegates = auditLogPorts.stream().toList();
        if (delegates.size() == 1) {
            return new AuditRecorder(delegates.get(0), contextEnricher);
        }
        return new AuditRecorder(new CompositeAuditLogPort(delegates), contextEnricher);
    }
}
