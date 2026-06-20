package com.indigo.synapse.oauth2.authorization.autoconfigure;

import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import com.indigo.synapse.oauth2.authorization.jwk.SigningKeyProvider;
import com.indigo.synapse.oauth2.authorization.jwk.SynapseRsaKeyFactory;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseAuthorizationServerSupportAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseAuthorizationServerSupportAutoConfiguration.class));

    @Test
    void shouldStartWithoutSigningKeyByDefault() {
        runner.run(context -> assertThat(context).hasNotFailed()
                .doesNotHaveBean(RSAKey.class)
                .doesNotHaveBean(JwtEncoder.class));
    }

    @Test
    void shouldCreateCompleteSigningChainWithDevelopmentKey() {
        runner.withPropertyValues(
                "synapse.oauth2.authorization.development-key-enabled=true",
                "synapse.oauth2.authorization.key-id=test-key")
                .run(context -> assertThat(context).hasSingleBean(RSAKey.class)
                        .hasSingleBean(SigningKeyProvider.class)
                        .hasSingleBean(JwtEncoder.class));
    }

    @Test
    void shouldKeepUserSigningKey() {
        RSAKey custom = SynapseRsaKeyFactory.generate("custom-key");
        runner.withPropertyValues("synapse.oauth2.authorization.development-key-enabled=true")
                .withBean(RSAKey.class, () -> custom)
                .run(context -> assertThat(context.getBean(RSAKey.class)).isSameAs(custom));
    }

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
