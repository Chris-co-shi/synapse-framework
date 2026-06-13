package com.indigo.synapse.oauth2.autoconfigure;

import com.indigo.synapse.oauth2.jwk.SecurityKeyPolicy;
import com.indigo.synapse.oauth2.jwk.SynapseRsaKeyFactory;
import com.indigo.synapse.oauth2.jwt.SynapseJwtService;
import com.indigo.synapse.oauth2.token.NoopTokenDenylistPort;
import com.indigo.synapse.oauth2.token.TokenDenylistPort;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Synapse OAuth2 核心自动配置。
 *
 * <p>只注册 JWT/JWK 基础 Bean，不创建 Web Security 过滤链，也不启用
 * Resource Server 或 Authorization Server。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseOAuth2Properties.class)
public class SynapseOAuth2AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RSAKey synapseRsaKey(SynapseOAuth2Properties properties) {
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
    public SynapseJwtService synapseJwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, SynapseOAuth2Properties properties) {
        return new SynapseJwtService(jwtEncoder, jwtDecoder, properties.getKeyId());
    }

    @Bean
    @ConditionalOnMissingBean(TokenDenylistPort.class)
    public TokenDenylistPort synapseTokenDenylistPort(SynapseOAuth2Properties properties) {
        if (properties.isProduction()) {
            throw new IllegalStateException("production environment must provide TokenDenylistPort; NoopTokenDenylistPort is not allowed");
        }
        return new NoopTokenDenylistPort();
    }
}
