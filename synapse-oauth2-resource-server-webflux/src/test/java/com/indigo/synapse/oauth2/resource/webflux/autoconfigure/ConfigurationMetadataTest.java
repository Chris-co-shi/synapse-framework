package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import org.junit.jupiter.api.Test;

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
}
