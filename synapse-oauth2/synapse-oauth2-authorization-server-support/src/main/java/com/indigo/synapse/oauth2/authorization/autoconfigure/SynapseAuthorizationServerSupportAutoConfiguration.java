package com.indigo.synapse.oauth2.authorization.autoconfigure;

import com.indigo.synapse.oauth2.authorization.jwk.SigningKeyPolicy;
import com.indigo.synapse.oauth2.authorization.jwk.SigningKeyProvider;
import com.indigo.synapse.oauth2.authorization.jwk.SigningKeySetProvider;
import com.indigo.synapse.oauth2.authorization.jwk.SynapseRsaKeyFactory;
import com.indigo.synapse.oauth2.authorization.jwt.SynapseJwtIssuer;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * OAuth2 JWT 签发支持自动配置。
 *
 * <p>该模块只提供私钥、JWKSource、JwtEncoder 和 JWT 签发器，不实现登录、
 * Authorization Server 协议端点、RegisteredClient 管理或 IAM 业务。</p>
 */
@AutoConfiguration
@ConditionalOnClass({JwtEncoder.class, RSAKey.class})
@EnableConfigurationProperties(SynapseJwtSigningProperties.class)
public class SynapseAuthorizationServerSupportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "synapse.oauth2.authorization",
            name = "development-key-enabled",
            havingValue = "true"
    )
    public RSAKey synapseDevelopmentRsaKey(SynapseJwtSigningProperties properties) {
        SigningKeyPolicy.validateSigningKeyId(properties.getKeyId(), properties.isProduction());
        if (properties.isProduction()) {
            throw new IllegalStateException("production environment must not generate development RSA signing key");
        }
        return SynapseRsaKeyFactory.generate(properties.getKeyId());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(RSAKey.class)
    public SigningKeyProvider signingKeyProvider(RSAKey rsaKey) {
        return () -> rsaKey;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(SigningKeyProvider.class)
    public SigningKeySetProvider signingKeySetProvider(SigningKeyProvider signingKeyProvider) {
        return () -> new JWKSet(signingKeyProvider.signingKey());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(SigningKeySetProvider.class)
    public JWKSource<SecurityContext> synapseSigningJwkSource(SigningKeySetProvider signingKeySetProvider) {
        return new ImmutableJWKSet<>(signingKeySetProvider.signingKeySet());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JWKSource.class)
    public JwtEncoder synapseJwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JwtEncoder.class)
    public SynapseJwtIssuer synapseJwtIssuer(JwtEncoder jwtEncoder, SynapseJwtSigningProperties properties) {
        SigningKeyPolicy.validateSigningKeyId(properties.getKeyId(), properties.isProduction());
        return new SynapseJwtIssuer(jwtEncoder, properties.getKeyId());
    }
}
