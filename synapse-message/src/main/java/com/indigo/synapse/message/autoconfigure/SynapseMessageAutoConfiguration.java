package com.indigo.synapse.message.autoconfigure;

import com.indigo.synapse.message.context.OperationContextMessageCodec;
import com.indigo.synapse.message.context.OperationContextMessagePropagator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SynapseMessageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessageCodec synapseOperationContextMessageCodec() {
        return new OperationContextMessageCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextMessagePropagator synapseOperationContextMessagePropagator(
            OperationContextMessageCodec codec
    ) {
        return new OperationContextMessagePropagator(codec);
    }
}
