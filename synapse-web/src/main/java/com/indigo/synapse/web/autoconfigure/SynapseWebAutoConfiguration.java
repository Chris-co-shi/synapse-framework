package com.indigo.synapse.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.web.openapi.OpenApiProperties;
import com.indigo.synapse.web.openapi.OpenApiVisibilityPolicy;
import com.indigo.synapse.web.json.SynapseObjectMapperFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Web 基础自动配置。
 *
 * <p>该配置提供不依赖具体 Web 栈的基础 Bean，例如统一 ObjectMapper 和 OpenAPI 可见性属性。
 * MVC 专属 Filter 配置位于 {@link SynapseWebMvcAutoConfiguration}。</p>
 */
@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration")
public class SynapseWebAutoConfiguration {

    /**
     * 提供 Synapse 默认 ObjectMapper。
     *
     * <p>如果消费方已经提供 ObjectMapper，本默认 Bean 不会覆盖。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper synapseObjectMapper() {
        return SynapseObjectMapperFactory.create();
    }

    /**
     * 创建 OpenAPI 可见性属性。
     *
     * <p>该属性仅表达 Synapse 默认策略，不直接注册 OpenAPI UI 或文档端点。</p>
     */
    @Bean
    public OpenApiProperties synapseOpenApiProperties(Environment environment) {
        OpenApiProperties defaults = OpenApiProperties.defaults();
        String activeProfile = environment.getActiveProfiles().length == 0 ? null : environment.getActiveProfiles()[0];
        boolean visible = OpenApiVisibilityPolicy.visible(defaults, activeProfile);
        return new OpenApiProperties(visible, defaults.title(), defaults.version());
    }
}
