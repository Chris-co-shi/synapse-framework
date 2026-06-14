package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.header.TrustedHeaderAuthenticatedUserResolver;
import com.indigo.synapse.security.header.TrustedHeaderSignatureVerifier;
import com.indigo.synapse.security.header.TrustedHeaderTimestampValidator;
import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import com.indigo.synapse.security.web.TrustedHeaderAuthenticationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security 模块自动配置。
 *
 * <p>本配置只提供轻量安全上下文、密码编码器和 trusted-header Servlet Filter。
 * 它不创建 Spring Security FilterChain，也不引入资源服务器或登录能力。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseSecurityProperties.class)
public class SynapseSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder synapsePasswordEncoder() {
        return SynapsePasswordEncoderFactory.bcrypt();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "synapse.security.trusted-header", name = "enabled", havingValue = "true")
    public TrustedHeaderAuthenticationFilter trustedHeaderAuthenticationFilter(SynapseSecurityProperties properties) {
        properties.validateTrustedHeaderConfiguration();
        return new TrustedHeaderAuthenticationFilter(
                properties,
                new TrustedHeaderAuthenticatedUserResolver(),
                new TrustedHeaderSignatureVerifier(),
                new TrustedHeaderTimestampValidator()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "trustedHeaderAuthenticationFilterRegistration")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "synapse.security.trusted-header", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<TrustedHeaderAuthenticationFilter> trustedHeaderAuthenticationFilterRegistration(
            TrustedHeaderAuthenticationFilter filter) {
        FilterRegistrationBean<TrustedHeaderAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("trustedHeaderAuthenticationFilter");
        registration.setOrder(-100);
        return registration;
    }
}
