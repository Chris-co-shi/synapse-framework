package com.indigo.synapse.messaging.autoconfigure;

import com.indigo.synapse.messaging.context.OperationContextMessagePropagator;
import com.indigo.synapse.messaging.producer.BestEffortMessagePublisher;
import com.indigo.synapse.messaging.transport.MessageTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/** 在所有 Transport 自动配置之后创建尽力发布器。 */
@AutoConfiguration(after = {SynapseMessagingAutoConfiguration.class, SynapseMessagingStreamAutoConfiguration.class})
@ConditionalOnProperty(prefix = "synapse.messaging", name = "enabled", matchIfMissing = true)
public class SynapseMessagingPublisherAutoConfiguration {
    @Bean
    @ConditionalOnBean(MessageTransport.class)
    @ConditionalOnMissingBean
    BestEffortMessagePublisher synapseBestEffortMessagePublisher(
            MessageTransport transport, OperationContextMessagePropagator propagator) {
        return new BestEffortMessagePublisher(transport, propagator);
    }
}
