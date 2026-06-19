package com.indigo.synapse.webmvc.autoconfigure;

import com.indigo.synapse.web.core.error.CommonErrorHttpStatusResolver;
import com.indigo.synapse.web.core.error.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.web.core.error.ErrorHttpStatusResolver;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpServletResponse.class)
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
