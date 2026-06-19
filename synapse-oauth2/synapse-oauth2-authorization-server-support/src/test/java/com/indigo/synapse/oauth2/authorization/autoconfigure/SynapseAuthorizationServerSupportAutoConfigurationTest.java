package com.indigo.synapse.oauth2.authorization.autoconfigure;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseAuthorizationServerSupportAutoConfigurationTest {

    @Test
    void shouldNotGenerateDevelopmentKeyByDefault() {
        SynapseJwtSigningProperties properties = new SynapseJwtSigningProperties();

        assertThat(properties.isDevelopmentKeyEnabled()).isFalse();
    }

    @Test
    void shouldGenerateDevelopmentKeyOnlyWhenEnabledAndNonProduction() {
        SynapseJwtSigningProperties properties = new SynapseJwtSigningProperties();
        properties.setDevelopmentKeyEnabled(true);
        properties.setKeyId("local-key");
        SynapseAuthorizationServerSupportAutoConfiguration autoConfiguration =
                new SynapseAuthorizationServerSupportAutoConfiguration();

        RSAKey rsaKey = autoConfiguration.synapseDevelopmentRsaKey(properties);

        assertThat(rsaKey.getKeyID()).isEqualTo("local-key");
        assertThat(rsaKey.isPrivate()).isTrue();
    }

    @Test
    void shouldRejectDevelopmentKeyInProduction() {
        SynapseJwtSigningProperties properties = new SynapseJwtSigningProperties();
        properties.setDevelopmentKeyEnabled(true);
        properties.setProduction(true);

        SynapseAuthorizationServerSupportAutoConfiguration autoConfiguration =
                new SynapseAuthorizationServerSupportAutoConfiguration();

        assertThatThrownBy(() -> autoConfiguration.synapseDevelopmentRsaKey(properties))
                .isInstanceOf(IllegalStateException.class);
    }
}
