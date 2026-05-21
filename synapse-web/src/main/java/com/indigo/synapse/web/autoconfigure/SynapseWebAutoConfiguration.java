package com.indigo.synapse.web.autoconfigure;

import com.indigo.synapse.web.openapi.OpenApiProperties;
import com.indigo.synapse.web.openapi.OpenApiVisibilityPolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@AutoConfiguration
public class SynapseWebAutoConfiguration {

    @Bean
    public OpenApiProperties synapseOpenApiProperties(Environment environment) {
        OpenApiProperties defaults = OpenApiProperties.defaults();
        String activeProfile = environment.getActiveProfiles().length == 0 ? null : environment.getActiveProfiles()[0];
        boolean visible = OpenApiVisibilityPolicy.visible(defaults, activeProfile);
        return new OpenApiProperties(visible, defaults.title(), defaults.version());
    }
}
