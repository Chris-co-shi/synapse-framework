package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reactive JWT 到 Authentication 转换器。
 */
public final class SynapseReactiveJwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        AuthenticatedPrincipal principal = principal(jwt);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities(jwt), principal.principalId());
        authentication.setDetails(principal);
        return Mono.just(authentication);
    }

    private static AuthenticatedPrincipal principal(Jwt jwt) {
        String principalType = required(jwt, SynapseJwtClaimNames.PRINCIPAL_TYPE);
        String tenantId = jwt.getClaimAsString(SynapseJwtClaimNames.TENANT_ID);
        Set<String> roles = strings(jwt, SynapseJwtClaimNames.ROLES);
        Set<String> permissions = strings(jwt, SynapseJwtClaimNames.PERMISSIONS);

        if ("CLIENT".equals(principalType)) {
            String clientId = required(jwt, SynapseJwtClaimNames.CLIENT_ID);
            return new AuthenticatedClient(clientId, clientId, tenantId, roles, permissions);
        }

        if ("USER".equals(principalType)) {
            String userId = required(jwt, SynapseJwtClaimNames.SUBJECT);
            String username = jwt.getClaimAsString(SynapseJwtClaimNames.PREFERRED_USERNAME);
            return new AuthenticatedUser(
                    userId,
                    username == null || username.isBlank() ? userId : username,
                    tenantId,
                    roles,
                    permissions
            );
        }

        throw new IllegalArgumentException("unsupported principal_type: " + principalType);
    }

    private static Collection<SimpleGrantedAuthority> authorities(Jwt jwt) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        strings(jwt, SynapseJwtClaimNames.SCOPE).forEach(scope -> add(authorities, "SCOPE_", scope));
        strings(jwt, SynapseJwtClaimNames.ROLES).forEach(role -> add(authorities, "ROLE_", role));
        strings(jwt, SynapseJwtClaimNames.PERMISSIONS).forEach(permission -> add(authorities, "PERM_", permission));
        return authorities;
    }

    private static void add(List<SimpleGrantedAuthority> authorities, String prefix, String value) {
        if (value != null && !value.isBlank()) {
            String trimmed = value.trim();
            authorities.add(new SimpleGrantedAuthority(trimmed.startsWith(prefix) ? trimmed : prefix + trimmed));
        }
    }

    private static String required(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(claim + " must not be blank");
        }
        return value;
    }

    private static Set<String> strings(Jwt jwt, String claim) {
        Object value = jwt.getClaims().get(claim);
        if (value instanceof String string) {
            Set<String> values = new LinkedHashSet<>();
            for (String part : string.split(" ")) {
                if (!part.isBlank()) {
                    values.add(part.trim());
                }
            }
            return values;
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(part -> !part.isBlank())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        return Set.of();
    }
}
