package com.indigo.synapse.oauth2.resource.webflux.config;

import com.indigo.synapse.oauth2.resource.webflux.autoconfigure.SynapseReactiveResourceServerProperties;
import com.indigo.synapse.oauth2.resource.webflux.context.SynapseReactiveSecurityContextWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtAuthenticationConverter;
import com.indigo.synapse.oauth2.resource.webflux.web.SynapseServerAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webflux.web.SynapseServerAuthenticationEntryPoint;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * Reactive Resource Server 配置器。
 */
public final class SynapseResourceServerServerHttpSecurityConfigurer {

    private final SynapseReactiveResourceServerProperties properties;
    private final SynapseReactiveJwtAuthenticationConverter authenticationConverter;
    private final SynapseServerAuthenticationEntryPoint entryPoint;
    private final SynapseServerAccessDeniedHandler accessDeniedHandler;
    private final SynapseReactiveSecurityContextWebFilter bridgeFilter;
    private final GatewayProofWebFilter gatewayProofWebFilter;

    public SynapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            SynapseServerAuthenticationEntryPoint entryPoint,
            SynapseServerAccessDeniedHandler accessDeniedHandler,
            SynapseReactiveSecurityContextWebFilter bridgeFilter) {
        this(properties, authenticationConverter, entryPoint, accessDeniedHandler, bridgeFilter, null);
    }

    public SynapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            SynapseServerAuthenticationEntryPoint entryPoint,
            SynapseServerAccessDeniedHandler accessDeniedHandler,
            SynapseReactiveSecurityContextWebFilter bridgeFilter,
            GatewayProofWebFilter gatewayProofWebFilter) {
        this.properties = properties;
        this.authenticationConverter = authenticationConverter;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.bridgeFilter = bridgeFilter;
        this.gatewayProofWebFilter = gatewayProofWebFilter;
    }

    public ServerHttpSecurity configure(ServerHttpSecurity http) {
        if (!properties.isCsrfEnabled()) {
            http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        }
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        http.authorizeExchange(exchanges -> {
            properties.getPermitPaths().forEach(path -> exchanges.pathMatchers(path).permitAll());
            exchanges.anyExchange().authenticated();
        });
        http.oauth2ResourceServer(resourceServer -> resourceServer
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));
        if (gatewayProofWebFilter != null) {
            http.addFilterAt(gatewayProofWebFilter, SecurityWebFiltersOrder.HTTP_BASIC);
        }
        http.addFilterAfter(bridgeFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http;
    }
}
