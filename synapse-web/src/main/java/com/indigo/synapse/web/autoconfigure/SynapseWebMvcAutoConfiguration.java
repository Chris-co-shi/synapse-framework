package com.indigo.synapse.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indigo.synapse.web.exception.SynapseExceptionBridgeFilter;
import com.indigo.synapse.web.exception.WebExceptionResponseFactory;
import com.indigo.synapse.web.trace.MvcTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Servlet MVC 自动配置。
 *
 * <p>该自动配置只在 Servlet Filter 与 DispatcherServlet 存在时生效，用于注册 MVC 场景下的 trace filter
 * 和 Filter 阶段异常桥接 filter。一阶段不在 synapse-web 中提供 WebFlux / Gateway 自动配置。</p>
 */
@AutoConfiguration
@ConditionalOnClass(name = {
        "jakarta.servlet.Filter",
        "org.springframework.web.servlet.DispatcherServlet"
})
public class SynapseWebMvcAutoConfiguration {

    /**
     * 创建 Filter 阶段异常桥接器。
     *
     * <p>该 Bean 可被消费方自定义 Bean 覆盖，便于业务系统替换响应写出策略。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public SynapseExceptionBridgeFilter synapseExceptionBridgeFilter(
            ObjectMapper objectMapper,
            WebExceptionResponseFactory responseFactory) {
        return new SynapseExceptionBridgeFilter(objectMapper, responseFactory);
    }

    /**
     * 注册 Filter 阶段异常桥接器。
     *
     * <p>顺序由 {@link SynapseExceptionBridgeFilter#ORDER} 控制，必须早于 security trusted-header 等
     * 后续 Filter，才能捕获它们抛出的 SynapseException。</p>
     */
    @Bean
    @ConditionalOnMissingBean(name = "synapseExceptionBridgeFilterRegistration")
    public FilterRegistrationBean<SynapseExceptionBridgeFilter> synapseExceptionBridgeFilterRegistration(
            SynapseExceptionBridgeFilter filter) {
        FilterRegistrationBean<SynapseExceptionBridgeFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("synapseExceptionBridgeFilter");
        registration.setOrder(SynapseExceptionBridgeFilter.ORDER);
        registration.addUrlPatterns("/*");
        return registration;
    }

    /**
     * 创建 MVC trace filter。
     */
    @Bean
    @ConditionalOnMissingBean
    public MvcTraceFilter synapseMvcTraceFilter() {
        return new MvcTraceFilter();
    }
}
