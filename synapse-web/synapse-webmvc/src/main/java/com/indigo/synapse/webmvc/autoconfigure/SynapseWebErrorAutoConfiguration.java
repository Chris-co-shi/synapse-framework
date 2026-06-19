package com.indigo.synapse.webmvc.autoconfigure;

import com.indigo.synapse.webmvc.exception.CommonErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.ErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class SynapseWebErrorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommonErrorHttpStatusResolver.class)
    public CommonErrorHttpStatusResolver commonErrorHttpStatusResolver() {
        return new CommonErrorHttpStatusResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeErrorHttpStatusResolver compositeErrorHttpStatusResolver(
            List<ErrorHttpStatusResolver> resolvers) {
        return new CompositeErrorHttpStatusResolver(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebExceptionResponseFactory webExceptionResponseFactory(
            CompositeErrorHttpStatusResolver statusResolver) {
        return new WebExceptionResponseFactory(statusResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebErrorResponseWriter webErrorResponseWriter(ObjectMapper objectMapper) {
        return new WebErrorResponseWriter(objectMapper);
    }
}
