package com.indigo.synapse.oauth2.resource.webflux.config;

import com.indigo.synapse.oauth2.resource.webflux.autoconfigure.SynapseReactiveResourceServerProperties;
import com.indigo.synapse.oauth2.resource.webflux.context.ReactivePrincipalContextWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.gatewayproof.GatewayProofWebFilter;
import com.indigo.synapse.oauth2.resource.webflux.jwt.SynapseReactiveJwtAuthenticationConverter;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

/**
 * Reactive Resource Server 配置器。
 */
public final class SynapseResourceServerServerHttpSecurityConfigurer {

    private final SynapseReactiveResourceServerProperties properties;
    private final SynapseReactiveJwtAuthenticationConverter authenticationConverter;
    private final ServerAuthenticationEntryPoint entryPoint;
    private final ServerAccessDeniedHandler accessDeniedHandler;
    private final ReactivePrincipalContextWebFilter bridgeFilter;
    private final GatewayProofWebFilter gatewayProofWebFilter;

    public SynapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            ServerAuthenticationEntryPoint entryPoint,
            ServerAccessDeniedHandler accessDeniedHandler,
            ReactivePrincipalContextWebFilter bridgeFilter) {
        this(properties, authenticationConverter, entryPoint, accessDeniedHandler, bridgeFilter, null);
    }

    public SynapseResourceServerServerHttpSecurityConfigurer(
            SynapseReactiveResourceServerProperties properties,
            SynapseReactiveJwtAuthenticationConverter authenticationConverter,
            ServerAuthenticationEntryPoint entryPoint,
            ServerAccessDeniedHandler accessDeniedHandler,
            ReactivePrincipalContextWebFilter bridgeFilter,
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
            http.addFilterAt(gatewayProofWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        }
        http.addFilterAfter(bridgeFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http;
    }
}
