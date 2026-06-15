package com.indigo.synapse.i18n.autoconfigure;

import com.indigo.synapse.i18n.FixedLocaleResolver;
import com.indigo.synapse.i18n.I18nMessageResolver;
import com.indigo.synapse.i18n.I18nResourceLoader;
import com.indigo.synapse.i18n.InMemoryI18nResourceLoader;
import com.indigo.synapse.i18n.LocaleResolver;
import com.indigo.synapse.i18n.SimpleI18nMessageResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Synapse i18n 自动配置。
 *
 * <p>只提供国际化消息解析抽象和轻量默认实现，不提供 i18n-resource-center、后台维护或业务资源服务。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseI18nProperties.class)
public class SynapseI18nAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LocaleResolver synapseLocaleResolver(SynapseI18nProperties properties) {
        return new FixedLocaleResolver(properties.getDefaultLocale());
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nResourceLoader synapseI18nResourceLoader(SynapseI18nProperties properties) {
        return new InMemoryI18nResourceLoader(properties.getMessages());
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nMessageResolver synapseI18nMessageResolver(I18nResourceLoader resourceLoader) {
        return new SimpleI18nMessageResolver(resourceLoader);
    }
}
