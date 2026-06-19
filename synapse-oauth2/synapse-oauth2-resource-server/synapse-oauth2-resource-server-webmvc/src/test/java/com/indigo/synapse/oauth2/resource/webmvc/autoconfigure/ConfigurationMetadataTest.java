package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

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
        assertThat(metadata).contains("synapse.security.resource-server.audiences");
        assertThat(metadata).contains("是否启用 Servlet OAuth2 Resource Server 自动配置。");
        assertThat(metadata).contains("当前服务接受的 JWT audience 列表。");
        assertThat(metadata).contains("synapse.security.resource-server.accepted-token-types");
        assertThat(metadata).contains("ACCESS_TOKEN");
    }
}
