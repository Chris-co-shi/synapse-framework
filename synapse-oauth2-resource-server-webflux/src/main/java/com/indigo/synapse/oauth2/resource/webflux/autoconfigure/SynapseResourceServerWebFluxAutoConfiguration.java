package com.indigo.synapse.oauth2.resource.webflux.autoconfigure;

import com.indigo.synapse.oauth2.resource.webflux.config.SynapseResourceServerServerHttpSecurityConfigurer;
import com.indigo.synapse.oauth2.resource.webflux.context.SynapseReactiveSecurityContextWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofServerAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtAuthenticationConverter;
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
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import java.time.Clock;
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
    public SynapseReactiveSecurityContextWebFilter synapseReactiveSecurityContextWebFilter() {
        return new SynapseReactiveSecurityContextWebFilter();
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
    public ReactiveJwtDecoder synapseReactiveJwtDecoder(SynapseReactiveResourceServerProperties properties) {
        if (properties.getJwkSetUri() != null && !properties.getJwkSetUri().isBlank()) {
            return NimbusReactiveJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        }
        if (properties.getIssuerUri() != null && !properties.getIssuerUri().isBlank()) {
            return ReactiveJwtDecoders.fromIssuerLocation(properties.getIssuerUri());
        }
        throw new IllegalStateException("ReactiveJwtDecoder or key source must be configured for reactive resource server");
    }

    @Bean
    @ConditionalOnMissingBean
    public SynapseResourceServerServerHttpSecurityConfigurer synapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            SynapseServerAuthenticationEntryPoint entryPoint,
            SynapseServerAccessDeniedHandler accessDeniedHandler,
            SynapseReactiveSecurityContextWebFilter bridgeFilter,
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
