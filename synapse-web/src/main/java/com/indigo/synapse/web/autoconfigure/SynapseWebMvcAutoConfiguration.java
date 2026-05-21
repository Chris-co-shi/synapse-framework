package com.indigo.synapse.web.autoconfigure;

import com.indigo.synapse.web.trace.MvcTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = {
        "jakarta.servlet.Filter",
        "org.springframework.web.servlet.DispatcherServlet"
})
public class SynapseWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MvcTraceFilter synapseMvcTraceFilter() {
        return new MvcTraceFilter();
    }
}
