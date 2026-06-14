package com.indigo.synapse.mq.autoconfigure;

import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.mq.consumer.DefaultMessageExceptionClassifier;
import com.indigo.synapse.mq.consumer.MessageConsumeTemplate;
import com.indigo.synapse.mq.consumer.MessageExceptionClassifier;
import com.indigo.synapse.mq.context.OperationContextMessageCodec;
import com.indigo.synapse.mq.context.OperationContextMessagePropagator;
import com.indigo.synapse.mq.idempotent.MessageIdempotencyChecker;
import com.indigo.synapse.mq.idempotent.NoopMessageIdempotencyChecker;
import com.indigo.synapse.mq.producer.MessagePublishTemplate;
import com.indigo.synapse.mq.producer.MessagePublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Synapse MQ 自动配置。
 *
 * <p>当前只装配 MQ 契约、上下文传播和轻量默认实现，不绑定具体 MQ SDK，也不会连接外部 Broker。</p>
 */
@AutoConfiguration
public class SynapseMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperationContextProvider synapseOperationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessageCodec synapseOperationContextMessageCodec() {
        return new OperationContextMessageCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessagePropagator synapseOperationContextMessagePropagator(
            OperationContextMessageCodec codec,
            OperationContextProvider contextProvider
    ) {
        return new OperationContextMessagePropagator(codec, contextProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageExceptionClassifier synapseMessageExceptionClassifier() {
        return new DefaultMessageExceptionClassifier();
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageIdempotencyChecker synapseMessageIdempotencyChecker() {
        return new NoopMessageIdempotencyChecker();
    }

    @Bean
    @ConditionalOnBean(MessagePublisher.class)
    @ConditionalOnMissingBean
    public MessagePublishTemplate synapseMessagePublishTemplate(
            MessagePublisher publisher,
            OperationContextMessagePropagator propagator
    ) {
        return new MessagePublishTemplate(publisher, propagator);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageConsumeTemplate synapseMessageConsumeTemplate(
            OperationContextMessagePropagator propagator,
            MessageExceptionClassifier exceptionClassifier
    ) {
        return new MessageConsumeTemplate(propagator, exceptionClassifier);
    }
}
