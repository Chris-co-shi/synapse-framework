package com.indigo.synapse.config.autoconfigure;

import com.indigo.synapse.config.ConfigClient;
import com.indigo.synapse.config.ConfigParser;
import com.indigo.synapse.config.ConfigResolver;
import com.indigo.synapse.config.DefaultConfigResolver;
import com.indigo.synapse.config.InMemoryConfigClient;
import com.indigo.synapse.config.SimpleConfigParser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Synapse config 自动配置。
 *
 * <p>只装配配置抽象、轻量读取和解析能力，不提供 config-service、配置发布、审批或数据库表。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseConfigProperties.class)
public class SynapseConfigAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigClient synapseConfigClient(SynapseConfigProperties properties) {
        return new InMemoryConfigClient(properties.getValues());
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigParser synapseConfigParser() {
        return new SimpleConfigParser();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigResolver synapseConfigResolver(ConfigClient client, ConfigParser parser) {
        return new DefaultConfigResolver(client, parser);
    }
}
