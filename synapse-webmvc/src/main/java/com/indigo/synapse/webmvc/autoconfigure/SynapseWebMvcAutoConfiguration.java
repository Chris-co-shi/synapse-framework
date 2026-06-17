package com.indigo.synapse.webmvc.autoconfigure;

import com.indigo.synapse.webmvc.context.MvcOperationContextFilter;
import com.indigo.synapse.webmvc.exception.SynapseExceptionBridgeFilter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.trace.MvcTraceFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Servlet MVC 自动配置。
 *
 * <p>该自动配置只在 Servlet Filter 与 DispatcherServlet 存在时生效，用于注册 MVC 场景下的 trace filter
 * 和 Filter 阶段异常桥接 filter。一阶段不在 synapse-webmvc 中提供 WebFlux / Gateway 自动配置。</p>
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
            WebErrorResponseWriter responseWriter,
            WebExceptionResponseFactory responseFactory) {
        return new SynapseExceptionBridgeFilter(responseWriter, responseFactory);
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

    /**
     * 创建 MVC OperationContext 恢复 filter。
     */
    @Bean
    @ConditionalOnMissingBean
    public MvcOperationContextFilter synapseMvcOperationContextFilter() {
        return new MvcOperationContextFilter();
    }
}
