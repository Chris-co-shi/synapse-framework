package com.indigo.synapse.starter.autoconfigure;

import com.indigo.synapse.starter.properties.SynapseBootProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SynapseBootProperties.class)
public class SynapseStarterAutoConfiguration {

    @Bean
    public SynapseAutoConfigurationPlan synapseAutoConfigurationPlan(SynapseBootProperties properties) {
        return new SynapseAutoConfigurationPlan(properties.toStarterProperties());
    }
}
