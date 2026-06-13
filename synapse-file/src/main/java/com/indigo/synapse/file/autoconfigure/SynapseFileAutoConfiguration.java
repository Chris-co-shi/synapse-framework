package com.indigo.synapse.file.autoconfigure;

import com.indigo.synapse.file.storage.FileStorage;
import com.indigo.synapse.file.storage.local.LocalFileStorage;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SynapseFileProperties.class)
public class SynapseFileAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileStorage synapseFileStorage(SynapseFileProperties properties) {
        return new LocalFileStorage(properties.getLocalRoot());
    }
}
