package com.indigo.synapse.message.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.message.core.ExponentialBackoffRetryPolicy;
import com.indigo.synapse.message.core.RetryPolicy;
import com.indigo.synapse.message.execution.CompensationHandler;
import com.indigo.synapse.message.execution.CompensationService;
import com.indigo.synapse.message.execution.DeadLetterService;
import com.indigo.synapse.message.execution.OutboxAppender;
import com.indigo.synapse.message.execution.ReliableMessageDispatcher;
import com.indigo.synapse.message.execution.ReliableMessageScheduler;
import com.indigo.synapse.message.infrastructure.persistence.converter.ReliableMessagePersistenceConverter;
import com.indigo.synapse.message.infrastructure.persistence.mapper.CompensationTaskMapper;
import com.indigo.synapse.message.infrastructure.persistence.mapper.DeadLetterMessageMapper;
import com.indigo.synapse.message.infrastructure.persistence.mapper.ReliableMessageMapper;
import com.indigo.synapse.message.infrastructure.persistence.repository.MybatisCompensationRepository;
import com.indigo.synapse.message.infrastructure.persistence.repository.MybatisDeadLetterRepository;
import com.indigo.synapse.message.infrastructure.persistence.repository.MybatisReliableMessageRepository;
import com.indigo.synapse.message.port.CompensationRepository;
import com.indigo.synapse.message.port.DeadLetterRepository;
import com.indigo.synapse.message.port.MessageTransport;
import com.indigo.synapse.message.port.ReliableMessageRepository;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.Collection;

/**
 * 可靠消息自动配置。
 */
@AutoConfiguration
@ConditionalOnClass({SqlSessionFactory.class, com.baomidou.mybatisplus.core.mapper.BaseMapper.class})
@ConditionalOnBean({DataSource.class, SqlSessionFactory.class})
@ConditionalOnProperty(prefix = "synapse.message.reliable", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SynapseReliableMessageProperties.class)
@MapperScan("com.indigo.synapse.message.infrastructure.persistence.mapper")
public class SynapseReliableMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock synapseMessageClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReliableMessagePersistenceConverter synapseReliableMessagePersistenceConverter(
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        return new ReliableMessagePersistenceConverter(objectMapperProvider.getIfAvailable(ObjectMapper::new));
    }

    @Bean
    @ConditionalOnMissingBean
    public ReliableMessageRepository synapseReliableMessageRepository(
            ReliableMessageMapper mapper,
            ReliableMessagePersistenceConverter converter
    ) {
        return new MybatisReliableMessageRepository(mapper, converter);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeadLetterRepository synapseDeadLetterRepository(DeadLetterMessageMapper mapper) {
        return new MybatisDeadLetterRepository(mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CompensationRepository synapseCompensationRepository(CompensationTaskMapper mapper) {
        return new MybatisCompensationRepository(mapper);
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
    public OutboxAppender synapseOutboxAppender(ReliableMessageRepository repository, Clock clock) {
        return new OutboxAppender(repository, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public DeadLetterService synapseDeadLetterService(ReliableMessageRepository repository, Clock clock) {
        return new DeadLetterService(repository, clock);
    }

    @Bean
    @ConditionalOnMissingBean
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
