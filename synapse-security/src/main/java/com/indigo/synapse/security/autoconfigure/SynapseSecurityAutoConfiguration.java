package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.header.TrustedHeaderAuthenticatedUserResolver;
import com.indigo.synapse.security.header.TrustedHeaderSignatureVerifier;
import com.indigo.synapse.security.header.TrustedHeaderTimestampValidator;
import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import com.indigo.synapse.security.permission.DefaultPermissionChecker;
import com.indigo.synapse.security.permission.PermissionChecker;
import com.indigo.synapse.security.permission.RequirePermissionAspect;
import com.indigo.synapse.security.web.TrustedHeaderAuthenticationFilter;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
 * <p>本配置只提供轻量安全上下文、密码编码器、PermissionChecker、RequirePermission AOP 适配器和
 * trusted-header Servlet Filter。它不创建 Spring Security FilterChain，也不提供 OAuth2 Resource Server、
 * 登录认证、授权后台或用户数据加载能力。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseSecurityProperties.class)
public class SynapseSecurityAutoConfiguration {

    /**
     * 默认密码编码器。
     *
     * <p>仅依赖 spring-security-crypto，不引入 spring-security-web。业务系统提供 PasswordEncoder Bean 时，
     * 默认 Bean 不覆盖。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder synapsePasswordEncoder() {
        return SynapsePasswordEncoderFactory.bcrypt();
    }

    /**
     * 默认权限检查器。
     */
    @Bean
    @ConditionalOnMissingBean(PermissionChecker.class)
    public PermissionChecker permissionChecker() {
        return new DefaultPermissionChecker();
    }

    /**
     * RequirePermission 声明式权限检查适配器。
     *
     * <p>该 Bean 只在 Spring AOP MethodInterceptor 存在、PermissionChecker 存在且注解开关开启时注册。</p>
     */
    @Bean
    @ConditionalOnClass(MethodInterceptor.class)
    @ConditionalOnBean(PermissionChecker.class)
    @ConditionalOnMissingBean(RequirePermissionAspect.class)
    @ConditionalOnProperty(
            prefix = "synapse.security.permission",
            name = "annotation-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public RequirePermissionAspect requirePermissionAspect(PermissionChecker permissionChecker) {
        return new RequirePermissionAspect(permissionChecker);
    }

    /**
     * Spring AOP 自动代理创建器。
     *
     * <p>只有在业务系统没有提供自己的 AutoProxyCreator 时才注册，避免覆盖消费方代理策略。</p>
     */
    @Bean
    @ConditionalOnClass(DefaultAdvisorAutoProxyCreator.class)
    @ConditionalOnMissingBean(AbstractAutoProxyCreator.class)
    @ConditionalOnProperty(
            prefix = "synapse.security.permission",
            name = "annotation-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public static DefaultAdvisorAutoProxyCreator requirePermissionAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * trusted-header 认证 Filter。
     *
     * <p>该 Filter 默认不启用，必须显式配置 synapse.security.trusted-header.enabled=true。</p>
     */
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

    /**
     * 注册 trusted-header 认证 Filter。
     *
     * <p>order=-100，预期晚于 synapse-web 的异常桥接 Filter，早于业务 Controller。</p>
     */
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
