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
 * <p>该自动配置只注册 JWT/JWK 基础 Bean，包括 RSAKey、JWKSource、JwtEncoder、JwtDecoder、
 * SynapseJwtService 和 TokenDenylistPort。它不创建 Spring Security FilterChain，不启用 Resource Server，
 * 也不提供 Authorization Server、登录页、客户端管理或用户授权流程。</p>
 *
 * <p>生产环境必须由应用提供真实签名密钥和 TokenDenylistPort；默认开发密钥和 NoopTokenDenylistPort
 * 只允许非生产环境使用。</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(SynapseOAuth2Properties.class)
public class SynapseOAuth2AutoConfiguration {

    /**
     * 创建开发环境 RSA 签名密钥。
     *
     * <p>生产环境不允许自动生成签名密钥，必须由消费方显式提供 RSAKey Bean，避免重启后 token 无法验证
     * 或默认密钥进入生产环境。</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public RSAKey synapseRsaKey(SynapseOAuth2Properties properties) {
        SecurityKeyPolicy.validateSigningKeyId(properties.getKeyId(), properties.isProduction());
        if (properties.isProduction()) {
            throw new IllegalStateException("production signing key material must be provided by the application");
        }
        return SynapseRsaKeyFactory.generate(properties.getKeyId());
    }

    /**
     * 基于 RSAKey 创建 JWKSource。
     */
    @Bean
    @ConditionalOnMissingBean
    public JWKSource<SecurityContext> synapseJwkSource(RSAKey synapseRsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(synapseRsaKey));
    }

    /**
     * 创建 JWT 编码器。
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtEncoder synapseJwtEncoder(JWKSource<SecurityContext> synapseJwkSource) {
        return new NimbusJwtEncoder(synapseJwkSource);
    }

    /**
     * 创建 JWT 解码器。
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder synapseJwtDecoder(RSAKey synapseRsaKey) throws Exception {
        return NimbusJwtDecoder.withPublicKey(synapseRsaKey.toRSAPublicKey()).build();
    }

    /**
     * 创建 Synapse JWT 服务。
     */
    @Bean
    @ConditionalOnMissingBean
    public SynapseJwtService synapseJwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, SynapseOAuth2Properties properties) {
        return new SynapseJwtService(jwtEncoder, jwtDecoder, properties.getKeyId());
    }

    /**
     * 创建默认 Token denylist 端口。
     *
     * <p>Noop 实现不会真正吊销 token，只适合开发或测试。生产环境必须提供 Redis、DB 或其他持久化实现。</p>
     */
    @Bean
    @ConditionalOnMissingBean(TokenDenylistPort.class)
    public TokenDenylistPort synapseTokenDenylistPort(SynapseOAuth2Properties properties) {
        if (properties.isProduction()) {
            throw new IllegalStateException("production environment must provide TokenDenylistPort; NoopTokenDenylistPort is not allowed");
        }
        return new NoopTokenDenylistPort();
    }
}
