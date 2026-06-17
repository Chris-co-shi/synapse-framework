package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseResourceServerPropertiesTest {

    @Test
    void shouldRequireIssuerWhenIssuerValidationEnabled() {
        SynapseResourceServerProperties properties = new SynapseResourceServerProperties();
        properties.setAudiences(List.of("service-a"));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer-uri");
    }

    @Test
    void shouldRejectJwkSetUriAndPublicKeyTogether() {
        SynapseResourceServerProperties properties = new SynapseResourceServerProperties();
        properties.setIssuerUri("https://issuer");
        properties.setAudiences(List.of("service-a"));
        properties.setJwkSetUri("https://issuer/jwks");
        properties.setPublicKeyLocation(new org.springframework.core.io.ByteArrayResource(new byte[0]));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be configured together");
    }
}
