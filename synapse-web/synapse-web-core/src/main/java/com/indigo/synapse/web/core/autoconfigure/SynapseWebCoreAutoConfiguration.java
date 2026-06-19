package com.indigo.synapse.web.core.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Web 技术栈共享的 Jackson 定制。
 *
 * <p>该自动配置只贡献 Jackson Module 和 Builder Customizer，不创建全局 ObjectMapper，
 * 因此 Spring Boot 的 {@code spring.jackson.*}、用户 ObjectMapper、用户 Module 和用户
 * Customizer 都能继续参与标准构建链。</p>
 */
@AutoConfiguration
@ConditionalOnClass({ObjectMapper.class, Jackson2ObjectMapperBuilderCustomizer.class})
public class SynapseWebCoreAutoConfiguration {

    /**
     * 在消费方没有提供 Java Time Module 时补齐 Java 时间类型支持。
     *
     * @return Java Time Module
     */
    @Bean
    @ConditionalOnMissingBean(JavaTimeModule.class)
    public JavaTimeModule synapseWebJavaTimeModule() {
        return new JavaTimeModule();
    }

    /**
     * 提供 Synapse 的保守 JSON 默认值，同时保留 Boot 和用户的其他 Jackson 定制。
     *
     * @return Builder 定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer synapseWebJacksonCustomizer() {
        return new SynapseWebJacksonCustomizer();
    }
}
