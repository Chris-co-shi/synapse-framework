package com.indigo.synapse.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.web.openapi.OpenApiProperties;
import com.indigo.synapse.web.openapi.OpenApiVisibilityPolicy;
import com.indigo.synapse.web.json.SynapseObjectMapperFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration(beforeName = "org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration")
public class SynapseWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper synapseObjectMapper() {
        return SynapseObjectMapperFactory.create();
    }

    @Bean
    public OpenApiProperties synapseOpenApiProperties(Environment environment) {
        OpenApiProperties defaults = OpenApiProperties.defaults();
        String activeProfile = environment.getActiveProfiles().length == 0 ? null : environment.getActiveProfiles()[0];
        boolean visible = OpenApiVisibilityPolicy.visible(defaults, activeProfile);
        return new OpenApiProperties(visible, defaults.title(), defaults.version());
    }
}
