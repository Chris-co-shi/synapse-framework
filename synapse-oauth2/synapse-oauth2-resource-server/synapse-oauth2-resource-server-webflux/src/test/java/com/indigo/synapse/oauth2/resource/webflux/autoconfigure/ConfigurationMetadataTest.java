package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationMetadataTest {

    @Test
    void shouldGenerateConfigurationMetadata() throws Exception {
        String metadata = new String(getClass().getClassLoader()
                .getResourceAsStream("META-INF/spring-configuration-metadata.json")
                .readAllBytes(), StandardCharsets.UTF_8);

        assertThat(metadata).contains("synapse.security.resource-server.enabled");
        assertThat(metadata).contains("synapse.security.resource-server.issuer-uri");
        assertThat(metadata).contains("synapse.security.resource-server.jwk-set-uri");
        assertThat(metadata).contains("是否启用 Reactive OAuth2 Resource Server 自动配置。");
        assertThat(metadata).contains("无需认证即可访问的 WebFlux 路径。");
        assertThat(metadata).contains("是否启用 Spring Security CSRF 防护。");
    }

    @Test
    void shouldBackOffBySecurityHandlerInterfaces() throws Exception {
        ConditionalOnMissingBean entryPointCondition = SynapseResourceServerWebFluxAutoConfiguration.class
                .getMethod("synapseServerAuthenticationEntryPoint",
                        com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory.class,
                        com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter.class)
                .getAnnotation(ConditionalOnMissingBean.class);
        ConditionalOnMissingBean deniedCondition = SynapseResourceServerWebFluxAutoConfiguration.class
                .getMethod("synapseServerAccessDeniedHandler",
                        com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory.class,
                        com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter.class)
                .getAnnotation(ConditionalOnMissingBean.class);

        assertThat(entryPointCondition.value()).containsExactly(ServerAuthenticationEntryPoint.class);
        assertThat(deniedCondition.value()).containsExactly(ServerAccessDeniedHandler.class);
    }
}
