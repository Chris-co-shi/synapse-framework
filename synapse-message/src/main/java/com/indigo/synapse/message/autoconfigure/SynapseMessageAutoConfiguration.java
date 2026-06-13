package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.execution.NoopDomainEventPublisher;
import com.indigo.synapse.message.publisher.DomainEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SynapseMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    public DomainEventPublisher synapseDomainEventPublisher() {
        return new NoopDomainEventPublisher();
    }
}
