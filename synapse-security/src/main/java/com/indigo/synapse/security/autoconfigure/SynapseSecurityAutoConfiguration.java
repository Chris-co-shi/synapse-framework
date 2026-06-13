package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration
@EnableConfigurationProperties(SynapseSecurityProperties.class)
public class SynapseSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder synapsePasswordEncoder() {
        return SynapsePasswordEncoderFactory.bcrypt();
    }
}
