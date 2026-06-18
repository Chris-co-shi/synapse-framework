package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import com.indigo.synapse.security.permission.DefaultPermissionChecker;
import com.indigo.synapse.security.permission.PermissionChecker;
import com.indigo.synapse.security.permission.RequirePermissionAspect;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security 模块自动配置。
 *
 * <p>本配置只提供 Web 无关的密码编码器、PermissionChecker 和 RequirePermission AOP 适配器。
 * 认证协议与 Web 请求入口由 OAuth2 Resource Server 等专用适配模块负责。</p>
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
     * RequirePermission 声明式权限检查自动代理创建器。
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(MethodInterceptor.class)
    @ConditionalOnMissingBean(DefaultAdvisorAutoProxyCreator.class)
    @ConditionalOnProperty(
            prefix = "synapse.security.permission",
            name = "annotation-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public static DefaultAdvisorAutoProxyCreator requirePermissionAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * RequirePermission 声明式权限检查 Advisor。
     *
     * <p>该 Bean 只在 Spring AOP MethodInterceptor 存在、PermissionChecker 存在且注解开关开启时注册。</p>
     *
     * <p>通过 {@link ObjectProvider} 延迟获取 PermissionChecker，避免 Advisor 在 BeanPostProcessor
     * 注册阶段被扫描时提前初始化权限检查器及其依赖。</p>
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnClass(MethodInterceptor.class)
    @ConditionalOnBean(PermissionChecker.class)
    @ConditionalOnMissingBean(RequirePermissionAspect.class)
    @ConditionalOnProperty(
            prefix = "synapse.security.permission",
            name = "annotation-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public static RequirePermissionAspect requirePermissionAspect(
            ObjectProvider<PermissionChecker> permissionCheckerProvider) {
        return new RequirePermissionAspect(permissionCheckerProvider);
    }
}
