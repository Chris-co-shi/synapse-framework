package com.indigo.synapse.security.autoconfigure;

import com.indigo.synapse.security.jwk.SecurityKeyPolicy;
import com.indigo.synapse.security.jwk.SynapseRsaKeyFactory;
import com.indigo.synapse.security.jwt.SynapseJwtService;
import com.indigo.synapse.security.oauth2.OAuth2PublicEndpointPolicy;
import com.indigo.synapse.security.password.SynapsePasswordEncoderFactory;
import com.indigo.synapse.security.token.NoopTokenDenylistPort;
import com.indigo.synapse.security.token.TokenDenylistPort;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
//import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
//import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
//import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
//import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@EnableConfigurationProperties(SynapseSecurityProperties.class)
public class SynapseSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RSAKey synapseRsaKey(SynapseSecurityProperties properties) {
        SecurityKeyPolicy.validateSigningKeyId(properties.getKeyId(), properties.isProduction());
        if (properties.isProduction()) {
            throw new IllegalStateException("production signing key material must be provided by the application");
        }
        return SynapseRsaKeyFactory.generate(properties.getKeyId());
    }

    @Bean
    @ConditionalOnMissingBean
    public JWKSource<SecurityContext> synapseJwkSource(RSAKey synapseRsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(synapseRsaKey));
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder synapseJwtEncoder(JWKSource<SecurityContext> synapseJwkSource) {
        return new NimbusJwtEncoder(synapseJwkSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder synapseJwtDecoder(RSAKey synapseRsaKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(synapseRsaKey.toRSAPublicKey()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseJwtService synapseJwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, SynapseSecurityProperties properties) {
        return new SynapseJwtService(jwtEncoder, jwtDecoder, properties.getKeyId());
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder synapsePasswordEncoder() {
        return SynapsePasswordEncoderFactory.bcrypt();
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public AuthorizationServerSettings synapseAuthorizationServerSettings(SynapseSecurityProperties properties) {
//        return AuthorizationServerSettings.builder()
//                .issuer(properties.getIssuer())
//                .build();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public RegisteredClientRepository synapseRegisteredClientRepository(SynapseSecurityProperties properties) {
//        if (properties.isProduction()) {
//            throw new IllegalStateException("production registered client repository must be provided by the application");
//        }
//        return new InMemoryRegisteredClientRepository();
//    }

//    @Bean
//    @ConditionalOnMissingBean
//    public OAuth2AuthorizationService synapseOAuth2AuthorizationService(RegisteredClientRepository registeredClientRepository) {
//        return new InMemoryOAuth2AuthorizationService();
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public OAuth2AuthorizationConsentService synapseOAuth2AuthorizationConsentService(RegisteredClientRepository registeredClientRepository) {
//        return new InMemoryOAuth2AuthorizationConsentService();
//    }

    @Bean
    @ConditionalOnMissingBean(TokenDenylistPort.class)
    public TokenDenylistPort synapseTokenDenylistPort(SynapseSecurityProperties properties) {
        if (properties.isProduction()) {
            throw new IllegalStateException("production token denylist port must be provided by the application");
        }
        return new NoopTokenDenylistPort();
    }

//    @Bean
//    @Order(1)
//    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//    @ConditionalOnMissingBean(name = "synapseAuthorizationServerSecurityFilterChain")
//    public SecurityFilterChain synapseAuthorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
//        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
//        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
//                .with(authorizationServerConfigurer, Customizer.withDefaults());
//        return http.build();
//    }

    @Bean
    @Order(2)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnMissingBean(name = "synapseResourceServerSecurityFilterChain")
    public SecurityFilterChain synapseResourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> {
                    for (String pattern : OAuth2PublicEndpointPolicy.publicPatterns()) {
                        authorize.requestMatchers(pattern).permitAll();
                    }
                    authorize.anyRequest().authenticated();
                })
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
