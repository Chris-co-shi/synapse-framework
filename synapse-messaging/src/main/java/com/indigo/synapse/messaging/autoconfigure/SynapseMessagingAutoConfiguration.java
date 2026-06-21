package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.messaging.consumer.MessageDispatcher;
import com.indigo.synapse.messaging.consumer.MessageHandler;
import com.indigo.synapse.messaging.consumer.MessageHandlerRegistry;
import com.indigo.synapse.messaging.context.OperationContextMessageCodec;
import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.producer.ReliableMessagePublisher;
import com.indigo.synapse.messaging.reliability.MessageFailureStore;
import com.indigo.synapse.messaging.reliability.MessageIdempotencyStore;
import com.indigo.synapse.messaging.reliability.MessageRetryPolicy;
import com.indigo.synapse.messaging.reliability.OutboxStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SynapseMessagingProperties.class)
@ConditionalOnProperty(prefix = "synapse.messaging", name = "enabled", matchIfMissing = true)
public class SynapseMessagingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    OperationContextProvider synapseMessagingOperationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    OperationContextMessageCodec synapseOperationContextMessageCodec() {
        return new OperationContextMessageCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    OperationContextMessagePropagator synapseOperationContextMessagePropagator(
            OperationContextMessageCodec codec, OperationContextProvider provider) {
        return new OperationContextMessagePropagator(codec, provider);
    }

    @Bean
    @ConditionalOnMissingBean
    MessageHandlerRegistry synapseMessageHandlerRegistry(ObjectProvider<MessageHandler> handlers) {
        return new MessageHandlerRegistry(handlers.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    MessageDispatcher synapseMessageDispatcher(
            MessageHandlerRegistry registry,
            OperationContextMessagePropagator propagator,
            ObjectProvider<MessageIdempotencyStore> stores,
            ObjectProvider<MessageFailureStore> failures,
            ObjectProvider<MessageRetryPolicy> policies,
            SynapseMessagingProperties properties) {
        MessageIdempotencyStore store = stores.getIfAvailable();
        return new MessageDispatcher(registry, propagator, store, failures.getIfUnique(), policies.getIfUnique(),
                properties.getConsumerId(), properties.getIdempotencyLease());
    }

    @Bean
    @ConditionalOnProperty(prefix = "synapse.messaging.reliable", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean
    ReliableMessagePublisher synapseReliableMessagePublisher(
            OutboxStore outboxStore, OperationContextMessagePropagator propagator) {
        return new ReliableMessagePublisher(outboxStore, propagator);
    }
}
