package com.indigo.synapse.security.webmvc;

import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.header.TrustedHeaderAuthenticatedUserResolver;
import com.indigo.synapse.security.header.TrustedHeaderSignatureVerifier;
import com.indigo.synapse.security.header.TrustedHeaderTimestampValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * trusted-header Servlet MVC 自动配置。
 *
 * <p>该模块只提供 Servlet Filter 适配，不创建 Spring Security FilterChain。</p>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SynapseSecurityWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.security.trusted-header", name = "enabled", havingValue = "true")
    public TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter(
            SynapseSecurityProperties properties,
            TrustedHeaderAuthenticatedUserResolver authenticatedUserResolver,
            TrustedHeaderSignatureVerifier signatureVerifier,
            TrustedHeaderTimestampValidator timestampValidator) {
        properties.validateTrustedHeaderConfiguration();
        return new TrustedHeaderAuthenticationFilter(
                properties,
                authenticatedUserResolver,
                signatureVerifier,
                timestampValidator
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "trustedHeaderAuthenticationFilterRegistration")
    @ConditionalOnProperty(prefix = "synapse.security.trusted-header", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<TrustedHeaderAuthenticationFilter> trustedHeaderAuthenticationFilterRegistration(
            TrustedHeaderAuthenticationFilter filter) {
        FilterRegistrationBean<TrustedHeaderAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("trustedHeaderAuthenticationFilter");
        registration.setOrder(-100);
        return registration;
    }
}
