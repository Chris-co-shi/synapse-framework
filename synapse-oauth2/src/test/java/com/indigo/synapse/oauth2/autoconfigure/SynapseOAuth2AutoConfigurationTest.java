package com.indigo.synapse.oauth2.autoconfigure;

import com.indigo.synapse.oauth2.jwt.SynapseJwtService;
import com.indigo.synapse.oauth2.token.NoopTokenDenylistPort;
import com.indigo.synapse.oauth2.token.TokenDenylistPort;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SynapseOAuth2AutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SynapseOAuth2AutoConfiguration.class));

    @Test
    void shouldRegisterCoreJwtAndJwkBeansByDefault() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(RSAKey.class));
            assertNotNull(context.getBean(JWKSource.class));
            assertNotNull(context.getBean(JwtEncoder.class));
            assertNotNull(context.getBean(JwtDecoder.class));
            assertNotNull(context.getBean(SynapseJwtService.class));
            assertInstanceOf(NoopTokenDenylistPort.class, context.getBean(TokenDenylistPort.class));
        });
    }

    @Test
    void shouldUseConfiguredKeyId() {
        contextRunner
                .withPropertyValues("synapse.oauth2.key-id=kid-test")
                .run(context -> assertEquals("kid-test", context.getBean(RSAKey.class).getKeyID()));
    }

    @Test
    void shouldFailInProductionWhenSigningKeyIsMissing() {
        contextRunner
                .withPropertyValues(
                        "synapse.oauth2.production=true",
                        "synapse.oauth2.key-id=prod-key"
                )
                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "production signing key material")));
    }

    @Test
    void shouldFailInProductionWhenTokenDenylistPortIsMissing() {
        contextRunner
                .withUserConfiguration(ProductionSigningKeyConfiguration.class)
                .withPropertyValues(
                        "synapse.oauth2.production=true",
                        "synapse.oauth2.key-id=prod-key"
                )
                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "production environment must provide TokenDenylistPort")));
    }

    @Configuration(proxyBeanMethods = false)
    static class ProductionSigningKeyConfiguration {

        @Bean
        RSAKey productionRsaKey() {
            return com.indigo.synapse.oauth2.jwk.SynapseRsaKeyFactory.generate("prod-key");
        }
    }

    private static boolean hasMessageInChain(Throwable failure, String expected) {
        Throwable current = failure;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
