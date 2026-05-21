package com.indigo.synapse.web.autoconfigure;

import com.indigo.synapse.web.trace.WebFluxTraceWebFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = {
        "org.springframework.web.reactive.DispatcherHandler",
        "org.springframework.web.server.WebFilter"
})
public class SynapseWebFluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WebFluxTraceWebFilter synapseWebFluxTraceWebFilter() {
        return new WebFluxTraceWebFilter();
    }
}
