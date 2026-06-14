package com.indigo.synapse.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.web.exception.SynapseExceptionBridgeFilter;
import com.indigo.synapse.web.trace.MvcTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(name = {
        "jakarta.servlet.Filter",
        "org.springframework.web.servlet.DispatcherServlet"
})
public class SynapseWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SynapseExceptionBridgeFilter synapseExceptionBridgeFilter(ObjectMapper objectMapper) {
        return new SynapseExceptionBridgeFilter(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(name = "synapseExceptionBridgeFilterRegistration")
    public FilterRegistrationBean<SynapseExceptionBridgeFilter> synapseExceptionBridgeFilterRegistration(
            SynapseExceptionBridgeFilter filter) {
        FilterRegistrationBean<SynapseExceptionBridgeFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("synapseExceptionBridgeFilter");
        registration.setOrder(SynapseExceptionBridgeFilter.ORDER);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public MvcTraceFilter synapseMvcTraceFilter() {
        return new MvcTraceFilter();
    }
}
