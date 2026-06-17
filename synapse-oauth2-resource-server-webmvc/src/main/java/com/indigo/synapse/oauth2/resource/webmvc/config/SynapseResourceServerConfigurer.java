package com.indigo.synapse.oauth2.resource.webmvc.config;

import com.indigo.synapse.oauth2.resource.webmvc.autoconfigure.SynapseResourceServerProperties;
import com.indigo.synapse.oauth2.resource.webmvc.context.SynapseSecurityContextBridgeFilter;
import com.indigo.synapse.oauth2.resource.webmvc.jwt.SynapseJwtAuthenticationConverter;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseAccessDeniedHandler;
import com.indigo.synapse.oauth2.resource.webmvc.web.SynapseBearerAuthenticationEntryPoint;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

/**
 * Servlet Resource Server 默认配置器。
 */
public final class SynapseResourceServerConfigurer {

    private final SynapseResourceServerProperties properties;
    private final SynapseJwtAuthenticationConverter authenticationConverter;
    private final SynapseBearerAuthenticationEntryPoint entryPoint;
    private final SynapseAccessDeniedHandler accessDeniedHandler;
    private final SynapseSecurityContextBridgeFilter bridgeFilter;

    public SynapseResourceServerConfigurer(
            SynapseResourceServerProperties properties,
            SynapseJwtAuthenticationConverter authenticationConverter,
            SynapseBearerAuthenticationEntryPoint entryPoint,
            SynapseAccessDeniedHandler accessDeniedHandler,
            SynapseSecurityContextBridgeFilter bridgeFilter) {
        this.properties = properties;
        this.authenticationConverter = authenticationConverter;
        this.entryPoint = entryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.bridgeFilter = bridgeFilter;
    }

    public HttpSecurity configure(HttpSecurity http) throws Exception {
        if (!properties.isCsrfEnabled()) {
            http.csrf(csrf -> csrf.disable());
        }
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler));
        http.authorizeHttpRequests(registry -> {
            properties.getPermitPaths().forEach(path -> registry.requestMatchers(path).permitAll());
            registry.anyRequest().authenticated();
        });
        http.oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter)));
        http.addFilterAfter(bridgeFilter, BearerTokenAuthenticationFilter.class);
        return http;
    }
}
