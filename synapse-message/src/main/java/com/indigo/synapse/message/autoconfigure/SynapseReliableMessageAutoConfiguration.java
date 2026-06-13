package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.core.ExponentialBackoffRetryPolicy;
import com.indigo.synapse.message.core.RetryPolicy;
import com.indigo.synapse.message.execution.CompensationHandler;
import com.indigo.synapse.message.execution.CompensationService;
import com.indigo.synapse.message.execution.DeadLetterService;
import com.indigo.synapse.message.execution.OutboxAppender;
import com.indigo.synapse.message.execution.ReliableMessageDispatcher;
import com.indigo.synapse.message.execution.ReliableMessageScheduler;
import com.indigo.synapse.message.port.CompensationRepository;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.MessageTransport;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.util.Collection;

/**
 * 可靠消息自动配置。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "synapse.message.reliable", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SynapseReliableMessageProperties.class)
public class SynapseReliableMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock synapseMessageClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryPolicy synapseMessageRetryPolicy(SynapseReliableMessageProperties properties) {
        SynapseReliableMessageProperties.Retry retry = properties.getRetry();
        return new ExponentialBackoffRetryPolicy(
                retry.getMaxAttempts(),
                retry.getInitialInterval(),
                retry.getMultiplier(),
                retry.getMaxInterval()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ReliableMessageRepository.class)
    public OutboxAppender synapseOutboxAppender(ReliableMessageRepository repository, Clock clock) {
        return new OutboxAppender(repository, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ReliableMessageRepository.class)
    public DeadLetterService synapseDeadLetterService(ReliableMessageRepository repository, Clock clock) {
        return new DeadLetterService(repository, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CompensationRepository.class)
    public CompensationService synapseCompensationService(
            CompensationRepository repository,
            Collection<CompensationHandler> handlers,
            Clock clock
    ) {
        return new CompensationService(repository, handlers, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MessageTransport.class)
    public ReliableMessageDispatcher synapseReliableMessageDispatcher(
            ReliableMessageRepository repository,
            DeadLetterRepository deadLetterRepository,
            MessageTransport messageTransport,
            RetryPolicy retryPolicy,
            Clock clock
    ) {
        return new ReliableMessageDispatcher(repository, deadLetterRepository, messageTransport, retryPolicy, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ReliableMessageDispatcher.class)
    @ConditionalOnProperty(prefix = "synapse.message.reliable.scheduler", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ReliableMessageScheduler synapseReliableMessageScheduler(
            ReliableMessageDispatcher dispatcher,
            SynapseReliableMessageProperties properties
    ) {
        return new ReliableMessageScheduler(dispatcher, properties);
    }
}
