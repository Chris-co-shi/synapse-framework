package com.indigo.synapse.webflux.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.web.core.error.CommonErrorHttpStatusResolver;
import com.indigo.synapse.web.core.error.CompositeErrorHttpStatusResolver;
import com.indigo.synapse.web.core.error.ErrorHttpStatusResolver;
import com.indigo.synapse.webflux.exception.SynapseWebFluxExceptionHandler;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import com.indigo.synapse.webflux.filter.SynapseWebFluxContextFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import java.util.List;

/**
 * WebFlux 技术支撑自动配置。
 *
 * <p>该配置只在 reactive web application 下生效，不提供 Gateway 路由、Gateway 鉴权业务或启动服务。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({ServerWebExchange.class, WebFilter.class})
public class SynapseWebFluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommonErrorHttpStatusResolver.class)
    public CommonErrorHttpStatusResolver webFluxCommonErrorHttpStatusResolver() {
        return new CommonErrorHttpStatusResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompositeErrorHttpStatusResolver webFluxCompositeErrorHttpStatusResolver(
            List<ErrorHttpStatusResolver> resolvers) {
        return new CompositeErrorHttpStatusResolver(resolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebFluxExceptionResponseFactory webFluxExceptionResponseFactory(
            CompositeErrorHttpStatusResolver statusResolver) {
        return new WebFluxExceptionResponseFactory(statusResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseWebFluxContextFilter synapseWebFluxContextFilter() {
        return new SynapseWebFluxContextFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseWebFluxExceptionHandler synapseWebFluxExceptionHandler(
            ReactiveWebErrorResponseWriter responseWriter,
            WebFluxExceptionResponseFactory responseFactory) {
        return new SynapseWebFluxExceptionHandler(responseWriter, responseFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveWebErrorResponseWriter reactiveWebErrorResponseWriter(ObjectMapper objectMapper) {
        return new ReactiveWebErrorResponseWriter(objectMapper);
    }
}
