package com.indigo.synapse.webmvc.autoconfigure;

import com.indigo.synapse.webmvc.openapi.OpenApiProperties;
import com.indigo.synapse.webmvc.openapi.OpenApiVisibilityPolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Web 基础自动配置。
 *
 * <p>该配置只提供 MVC 模块的 OpenAPI 可见性策略。共享 Jackson 定制由
 * synapse-web-core 提供，MVC 专属 Filter 配置位于 {@link SynapseWebMvcAutoConfiguration}。</p>
 */
@AutoConfiguration
public class SynapseWebAutoConfiguration {

    /**
     * 创建 OpenAPI 可见性属性。
     *
     * <p>该属性仅表达 Synapse 默认策略，不直接注册 OpenAPI UI 或文档端点。</p>
     */
    @Bean
    public OpenApiProperties synapseOpenApiProperties(Environment environment) {
        OpenApiProperties defaults = OpenApiProperties.defaults();
        String activeProfile = environment.getActiveProfiles().length == 0 ? null : environment.getActiveProfiles()[0];
        boolean visible = OpenApiVisibilityPolicy.visible(defaults, activeProfile);
        return new OpenApiProperties(visible, defaults.title(), defaults.version());
    }
}
