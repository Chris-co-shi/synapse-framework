//package com.indigo.synapse.security.autoconfigure;
//
//import com.indigo.synapse.security.jwk.SynapseRsaKeyFactory;
//import com.indigo.synapse.security.jwt.SynapseJwtService;
//import com.indigo.synapse.security.token.NoopTokenDenylistPort;
//import com.indigo.synapse.security.token.TokenDenylistPort;
//import com.nimbusds.jose.jwk.RSAKey;
//import com.nimbusds.jose.jwk.source.JWKSource;
//import com.nimbusds.jose.proc.SecurityContext;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.autoconfigure.AutoConfigurations;
//import org.springframework.boot.test.context.runner.ApplicationContextRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtEncoder;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
////import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//class SynapseSecurityAutoConfigurationTest {
//
//    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
//            .withConfiguration(AutoConfigurations.of(SynapseSecurityAutoConfiguration.class));
//
//    @Test
//    void shouldRegisterJwtJwkAndPasswordBeans() {
//        contextRunner
//                .withPropertyValues("synapse.security.key-id=kid-test")
//                .run(context -> {
//                    assertNotNull(context.getBean(RSAKey.class));
//                    assertNotNull(context.getBean(JWKSource.class));
//                    assertNotNull(context.getBean(JwtEncoder.class));
//                    assertNotNull(context.getBean(JwtDecoder.class));
//                    assertNotNull(context.getBean(SynapseJwtService.class));
//                    assertNotNull(context.getBean(AuthorizationServerSettings.class));
//                    assertNotNull(context.getBean(RegisteredClientRepository.class));
//                    assertNotNull(context.getBean(OAuth2AuthorizationService.class));
//                    assertNotNull(context.getBean(OAuth2AuthorizationConsentService.class));
//                    assertNotNull(context.getBean(TokenDenylistPort.class));
//                    assertTrue(context.getBean(PasswordEncoder.class).matches("secret", context.getBean(PasswordEncoder.class).encode("secret")));
//                });
//    }
//
//    @Test
//    void shouldRejectDefaultKeyIdInProduction() {
//        contextRunner
//                .withPropertyValues(
//                        "synapse.security.production=true",
//                        "synapse.security.key-id=synapse-dev"
//                )
//                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "default signing key id")));
//    }
//
//    @Test
//    void shouldRejectGeneratedSigningKeyInProduction() {
//        contextRunner
//                .withPropertyValues(
//                        "synapse.security.production=true",
//                        "synapse.security.key-id=prod-key"
//                )
//                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "production signing key material")));
//    }
//
//    @Test
//    void shouldRejectDefaultRegisteredClientRepositoryInProduction() {
//        contextRunner
//                .withUserConfiguration(ProductionKeyConfiguration.class)
//                .withPropertyValues(
//                        "synapse.security.production=true",
//                        "synapse.security.key-id=prod-key"
//                )
//                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "production registered client repository")));
//    }
//
//    @Test
//    void shouldRejectDefaultTokenDenylistPortInProduction() {
//        contextRunner
//                .withUserConfiguration(ProductionKeyConfiguration.class, ProductionRegisteredClientConfiguration.class)
//                .withPropertyValues(
//                        "synapse.security.production=true",
//                        "synapse.security.key-id=prod-key"
//                )
//                .run(context -> assertTrue(hasMessageInChain(context.getStartupFailure(), "production token denylist port")));
//    }
//
//    @Test
//    void shouldStartInProductionWhenProductionBeansAreProvided() {
//        contextRunner
//                .withUserConfiguration(
//                        ProductionKeyConfiguration.class,
//                        ProductionRegisteredClientConfiguration.class,
//                        ProductionTokenDenylistConfiguration.class
//                )
//                .withPropertyValues(
//                        "synapse.security.production=true",
//                        "synapse.security.key-id=prod-key"
//                )
//                .run(context -> {
//                    assertNotNull(context.getBean(SynapseJwtService.class));
//                    assertNotNull(context.getBean(RegisteredClientRepository.class));
//                    assertNotNull(context.getBean(TokenDenylistPort.class));
//                });
//    }
//
//    @Configuration(proxyBeanMethods = false)
//    static class ProductionKeyConfiguration {
//
//        @Bean
//        RSAKey productionRsaKey() {
//            return SynapseRsaKeyFactory.generate("prod-key");
//        }
//    }
//
//    @Configuration(proxyBeanMethods = false)
//    static class ProductionRegisteredClientConfiguration {
//
//        @Bean
//        RegisteredClientRepository productionRegisteredClientRepository() {
//            return new RegisteredClientRepository() {
//                @Override
//                public void save(RegisteredClient registeredClient) {
//                }
//
//                @Override
//                public RegisteredClient findById(String id) {
//                    return null;
//                }
//
//                @Override
//                public RegisteredClient findByClientId(String clientId) {
//                    return null;
//                }
//            };
//        }
//    }
//
//    @Configuration(proxyBeanMethods = false)
//    static class ProductionTokenDenylistConfiguration {
//
//        @Bean
//        TokenDenylistPort productionTokenDenylistPort() {
//            return new NoopTokenDenylistPort();
//        }
//    }
//
//    private static boolean hasMessageInChain(Throwable failure, String expected) {
//        Throwable current = failure;
//        while (current != null) {
//            if (current.getMessage() != null && current.getMessage().contains(expected)) {
//                return true;
//            }
//            current = current.getCause();
//        }
//        return false;
//    }
//}
