package com.indigo.synapse.oauth2.resource.webmvc.autoconfigure;

import com.indigo.synapse.oauth2.core.token.NoopTokenDenylistPort;
import com.indigo.synapse.oauth2.core.token.TokenDenylistPort;
import com.indigo.synapse.oauth2.core.validation.AudienceValidator;
import com.indigo.synapse.oauth2.core.validation.DenylistedTokenValidator;
import com.indigo.synapse.oauth2.core.validation.PrincipalClaimsValidator;
import com.indigo.synapse.oauth2.core.validation.PrincipalTypeClaimValidator;
import com.indigo.synapse.oauth2.core.validation.RequiredClaimsValidator;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidator;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidatorFactory;
import com.indigo.synapse.oauth2.core.validation.TokenTypeValidator;
import com.indigo.synapse.oauth2.resource.webmvc.config.SynapseResourceServerConfigurer;
import com.indigo.synapse.oauth2.resource.webmvc.context.SynapsePrincipalContextBridgeFilter;
import com.indigo.synapse.oauth2.resource.webmvc.gatewayproof.GatewayProofAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webmvc.gatewayproof.GatewayProofVerificationFilter;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationConverter;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseSpringJwtValidatorAdapter;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseBearerAuthenticationEntryPoint;
import com.indigo.synapse.webmvc.exception.WebErrorResponseWriter;
import com.indigo.synapse.webmvc.exception.WebExceptionResponseFactory;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProofReplayStore;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import com.indigo.synapse.security.gatewayproof.HmacSha256GatewayProofVerifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.time.Clock;
import java.util.Map;

/**
 * Servlet OAuth2 Resource Server 自动配置。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({HttpSecurity.class, JwtDecoder.class})
@ConditionalOnProperty(prefix = "synapse.security.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SynapseResourceServerProperties.class)
public class SynapseResourceServerWebMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SynapseJwtAuthenticationConverter synapseJwtAuthenticationConverter() {
        return new SynapseJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapsePrincipalContextBridgeFilter synapsePrincipalContextBridgeFilter() {
        return new SynapsePrincipalContextBridgeFilter();
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationEntryPoint.class)
    public SynapseBearerAuthenticationEntryPoint synapseBearerAuthenticationEntryPoint(
            WebExceptionResponseFactory responseFactory,
            WebErrorResponseWriter responseWriter) {
        return new SynapseBearerAuthenticationEntryPoint(responseFactory, responseWriter);
    }

    @Bean
    @ConditionalOnMissingBean(AccessDeniedHandler.class)
    public SynapseAccessDeniedHandler synapseAccessDeniedHandler(
            WebExceptionResponseFactory responseFactory,
            WebErrorResponseWriter responseWriter) {
        return new SynapseAccessDeniedHandler(responseFactory, responseWriter);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.security.gateway-proof", name = "enabled", havingValue = "true")
    public GatewayProofAccessDeniedHandler gatewayProofAccessDeniedHandler(
            WebExceptionResponseFactory responseFactory,
            WebErrorResponseWriter responseWriter) {
        return new GatewayProofAccessDeniedHandler(responseFactory, responseWriter);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.security.gateway-proof", name = "enabled", havingValue = "true")
    public GatewayProofVerifier gatewayProofVerifier(
            SynapseSecurityProperties securityProperties,
            ObjectProvider<GatewayProofReplayStore> replayStoreProvider) {
        SynapseSecurityProperties.GatewayProof gatewayProof = securityProperties.getGatewayProof();
        return new HmacSha256GatewayProofVerifier(
                Map.of(gatewayProof.getGatewayId(), gatewayProof.getSecret()),
                gatewayProof.getTimestampSkew(),
                Clock.systemUTC(),
                replayStoreProvider.getIfAvailable(),
                gatewayProof.isReplayProtectionEnabled(),
                gatewayProof.isFailFast()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.security.gateway-proof", name = "enabled", havingValue = "true")
    public GatewayProofVerificationFilter gatewayProofVerificationFilter(
            SynapseSecurityProperties securityProperties,
            GatewayProofVerifier gatewayProofVerifier,
            GatewayProofTokenHasher tokenHasher,
            GatewayProofAccessDeniedHandler accessDeniedHandler) {
        return new GatewayProofVerificationFilter(
                securityProperties.getGatewayProof(),
                gatewayProofVerifier,
                tokenHasher,
                accessDeniedHandler
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseJwtValidator synapseJwtValidator(
            SynapseResourceServerProperties properties,
            org.springframework.beans.factory.ObjectProvider<TokenDenylistPort> denylistPortProvider) {
        properties.validate();
        List<SynapseJwtValidator> validators = new ArrayList<>();
        validators.add(new RequiredClaimsValidator(properties.getRequiredClaims()));
        if (properties.isAudienceValidationEnabled()) {
            validators.add(new AudienceValidator(properties.getAudiences()));
        }
        validators.add(new TokenTypeValidator(properties.getAcceptedTokenTypes()));
        validators.add(new PrincipalTypeClaimValidator());
        validators.add(new PrincipalClaimsValidator());
        if (properties.isDenylistEnabled()) {
            TokenDenylistPort denylistPort = denylistPortProvider.getIfAvailable();
            if (denylistPort == null || denylistPort instanceof NoopTokenDenylistPort) {
                throw new IllegalStateException("real TokenDenylistPort is required when denylist is enabled");
            }
            validators.add(new DenylistedTokenValidator(denylistPort));
        }
        return SynapseJwtValidatorFactory.composite(validators);
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2TokenValidator<Jwt> synapseSpringJwtValidator(
            SynapseResourceServerProperties properties,
            SynapseJwtValidator synapseJwtValidator) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(nullToZero(properties.getClockSkew())));
        if (properties.isIssuerValidationEnabled()) {
            validators.add(new JwtIssuerValidator(properties.getIssuerUri()));
        }
        validators.add(new SynapseSpringJwtValidatorAdapter(synapseJwtValidator));
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder synapseJwtDecoder(
            SynapseResourceServerProperties properties,
            OAuth2TokenValidator<Jwt> validator) throws Exception {
        properties.validate();
        NimbusJwtDecoder decoder;
        if (properties.getPublicKeyLocation() != null) {
            try (InputStream inputStream = properties.getPublicKeyLocation().getInputStream()) {
                RSAPublicKey publicKey = RsaKeyConverters.x509().convert(inputStream);
                decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
            }
        } else if (properties.getJwkSetUri() != null && !properties.getJwkSetUri().isBlank()) {
            decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        } else if (properties.getIssuerUri() != null && !properties.getIssuerUri().isBlank()) {
            decoder = NimbusJwtDecoder.withIssuerLocation(properties.getIssuerUri()).build();
        } else {
            throw new IllegalStateException("JwtDecoder or key source must be configured for resource server");
        }
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseResourceServerConfigurer synapseResourceServerConfigurer(
            SynapseResourceServerProperties properties,
            SynapseJwtAuthenticationConverter authenticationConverter,
            SynapseBearerAuthenticationEntryPoint entryPoint,
            SynapseAccessDeniedHandler accessDeniedHandler,
            SynapsePrincipalContextBridgeFilter bridgeFilter,
            ObjectProvider<GatewayProofVerificationFilter> gatewayProofFilter) {
        return new SynapseResourceServerConfigurer(
                properties,
                authenticationConverter,
                entryPoint,
                accessDeniedHandler,
                bridgeFilter,
                gatewayProofFilter.getIfAvailable()
        );
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnBean(JwtDecoder.class)
    public SecurityFilterChain synapseResourceServerSecurityFilterChain(
            HttpSecurity http,
            SynapseResourceServerConfigurer configurer) throws Exception {
        return configurer.configure(http).build();
    }

    private static Duration nullToZero(Duration duration) {
        return duration == null ? Duration.ZERO : duration;
    }
}
