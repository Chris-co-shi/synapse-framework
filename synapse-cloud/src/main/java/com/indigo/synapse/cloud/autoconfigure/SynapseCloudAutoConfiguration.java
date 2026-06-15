package com.indigo.synapse.cloud.autoconfigure;

import com.indigo.synapse.cloud.context.OperationContextHttpHeaderCodec;
import com.indigo.synapse.cloud.remote.RemoteErrorBodyParser;
import com.indigo.synapse.core.context.DefaultOperationContextProvider;
import com.indigo.synapse.core.context.OperationContextProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Synapse Cloud 基础自动配置。
 *
 * <p>该自动配置只装配服务间调用所需的基础技术 Bean，不连接注册中心、配置中心或 Gateway。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseCloudProperties.class)
@ConditionalOnProperty(prefix = "synapse.cloud", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SynapseCloudAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OperationContextProvider synapseCloudOperationContextProvider() {
        return new DefaultOperationContextProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationContextHttpHeaderCodec synapseOperationContextHttpHeaderCodec() {
        return new OperationContextHttpHeaderCodec();
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteErrorBodyParser synapseRemoteErrorBodyParser() {
        return new RemoteErrorBodyParser();
    }
}
