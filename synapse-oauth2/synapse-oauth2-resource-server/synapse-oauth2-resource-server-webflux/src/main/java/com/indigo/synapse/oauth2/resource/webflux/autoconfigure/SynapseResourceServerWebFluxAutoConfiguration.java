package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import com.indigo.synapse.oauth2.core.token.TokenDenylistPort;
import com.indigo.synapse.oauth2.core.validation.SynapseJwtValidator;
import com.indigo.synapse.oauth2.resource.core.ResourceServerValidatorFactory;
import com.indigo.synapse.oauth2.resource.webflux.config.SynapseResourceServerServerHttpSecurityConfigurer;
import com.indigo.synapse.oauth2.resource.webflux.context.ReactivePrincipalContextWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofServerAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtAuthenticationConverter;
import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtValidatorAdapter;
import com.indigo.synapse.oauth2.resource.webflux.web.SynapseServerAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webflux.web.SynapseServerAuthenticationEntryPoint;
import com.indigo.synapse.webflux.exception.ReactiveWebErrorResponseWriter;
import com.indigo.synapse.webflux.exception.WebFluxExceptionResponseFactory;
import com.indigo.synapse.security.autoconfigure.SynapseSecurityProperties;
import com.indigo.synapse.security.gatewayproof.GatewayProofReplayStore;
import com.indigo.synapse.security.gatewayproof.GatewayProofTokenHasher;
import com.indigo.synapse.security.gatewayproof.GatewayProofVerifier;
import com.indigo.synapse.security.gatewayproof.HmacSha256GatewayProofVerifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.web.server.SecurityWebFilterChain;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reactive OAuth2 Resource Server 自动配置。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({ServerHttpSecurity.class, ReactiveJwtDecoder.class})
@ConditionalOnProperty(prefix = "synapse.security.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SynapseReactiveResourceServerProperties.class)
public class SynapseResourceServerWebFluxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SynapseReactiveJwtAuthenticationConverter synapseReactiveJwtAuthenticationConverter() {
        return new SynapseReactiveJwtAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseJwtValidator synapseReactiveSynapseJwtValidator(
            SynapseReactiveResourceServerProperties properties,
            ObjectProvider<TokenDenylistPort> denylistPortProvider) {
        return ResourceServerValidatorFactory.create(
                properties.toValidationPolicy(), denylistPortProvider.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public OAuth2TokenValidator<Jwt> synapseReactiveSpringJwtValidator(
            SynapseReactiveResourceServerProperties properties,
            SynapseJwtValidator validator) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(properties.getClockSkew()));
        if (properties.isIssuerValidationEnabled()) {
            validators.add(new JwtIssuerValidator(properties.getIssuerUri()));
        }
        validators.add(new SynapseReactiveJwtValidatorAdapter(validator));
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactivePrincipalContextWebFilter reactivePrincipalContextWebFilter() {
        return new ReactivePrincipalContextWebFilter();
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseServerAuthenticationEntryPoint synapseServerAuthenticationEntryPoint(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter) {
        return new SynapseServerAuthenticationEntryPoint(responseFactory, responseWriter);
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseServerAccessDeniedHandler synapseServerAccessDeniedHandler(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter) {
        return new SynapseServerAccessDeniedHandler(responseFactory, responseWriter);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "synapse.security.gateway-proof", name = "enabled", havingValue = "true")
    public GatewayProofServerAccessDeniedHandler gatewayProofServerAccessDeniedHandler(
            WebFluxExceptionResponseFactory responseFactory,
            ReactiveWebErrorResponseWriter responseWriter) {
        return new GatewayProofServerAccessDeniedHandler(responseFactory, responseWriter);
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
    public GatewayProofWebFilter gatewayProofWebFilter(
            SynapseSecurityProperties securityProperties,
            GatewayProofVerifier gatewayProofVerifier,
            GatewayProofTokenHasher tokenHasher,
            GatewayProofServerAccessDeniedHandler accessDeniedHandler) {
        return new GatewayProofWebFilter(
                securityProperties.getGatewayProof(),
                gatewayProofVerifier,
                tokenHasher,
                accessDeniedHandler
        );
    }

    @Bean
    @ConditionalOnMissingBean(ReactiveJwtDecoder.class)
    public ReactiveJwtDecoder synapseReactiveJwtDecoder(
            SynapseReactiveResourceServerProperties properties,
            OAuth2TokenValidator<Jwt> validator) {
        properties.validate();
        NimbusReactiveJwtDecoder decoder;
        if (properties.getJwkSetUri() != null && !properties.getJwkSetUri().isBlank()) {
            decoder = NimbusReactiveJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        } else if (properties.getIssuerUri() != null && !properties.getIssuerUri().isBlank()) {
            decoder = NimbusReactiveJwtDecoder.withIssuerLocation(properties.getIssuerUri()).build();
        } else {
            throw new IllegalStateException(
                    "ReactiveJwtDecoder or key source must be configured for reactive resource server");
        }
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseResourceServerServerHttpSecurityConfigurer synapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            SynapseServerAuthenticationEntryPoint entryPoint,
            SynapseServerAccessDeniedHandler accessDeniedHandler,
            ReactivePrincipalContextWebFilter bridgeFilter,
            ObjectProvider<GatewayProofWebFilter> gatewayProofWebFilter) {
        return new SynapseResourceServerServerHttpSecurityConfigurer(
                properties,
                authenticationConverter,
                entryPoint,
                accessDeniedHandler,
                bridgeFilter,
                gatewayProofWebFilter.getIfAvailable()
        );
    }

    @Bean
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    public SecurityWebFilterChain synapseSecurityWebFilterChain(
            ServerHttpSecurity http,
            SynapseResourceServerServerHttpSecurityConfigurer configurer) {
        return configurer.configure(http).build();
    }
}
