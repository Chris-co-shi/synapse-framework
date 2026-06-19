package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.jwt.JwtClaimValues;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Reactive JWT 到 Authentication 转换器。
 *
 * <p>JWT claim 的必填校验和字符串集合规范化统一委托给 OAuth2 Core，
 * 避免 Servlet 与 Reactive Resource Server 产生不同的协议解释。</p>
 */
public final class SynapseReactiveJwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        JwtClaimAccessor claims = new SpringJwtClaimAccessor(jwt);
        AuthenticatedPrincipal principal = principal(jwt, claims);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                authorities(claims),
                principal.principalId()
        );
        authentication.setDetails(principal);
        return Mono.just(authentication);
    }

    private static AuthenticatedPrincipal principal(Jwt jwt, JwtClaimAccessor claims) {
        String principalType = JwtClaimValues.requiredString(
                claims,
                SynapseJwtClaimNames.PRINCIPAL_TYPE
        );
        String tenantId = jwt.getClaimAsString(SynapseJwtClaimNames.TENANT_ID);
        Set<String> roles = JwtClaimValues.strings(claims, SynapseJwtClaimNames.ROLES);
        Set<String> permissions = JwtClaimValues.strings(claims, SynapseJwtClaimNames.PERMISSIONS);

        if (SynapsePrincipalType.CLIENT.name().equals(principalType)) {
            String clientId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.CLIENT_ID);
            return new AuthenticatedClient(clientId, clientId, tenantId, roles, permissions);
        }

        if (SynapsePrincipalType.USER.name().equals(principalType)) {
            String userId = JwtClaimValues.requiredString(claims, SynapseJwtClaimNames.SUBJECT);
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

    private static Collection<SimpleGrantedAuthority> authorities(JwtClaimAccessor claims) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.SCOPE)
                .forEach(scope -> add(authorities, "SCOPE_", scope));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.ROLES)
                .forEach(role -> add(authorities, "ROLE_", role));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.PERMISSIONS)
                .forEach(permission -> add(authorities, "PERM_", permission));
        return List.copyOf(authorities);
    }

    private static void add(List<SimpleGrantedAuthority> authorities, String prefix, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String trimmed = value.trim();
        authorities.add(new SimpleGrantedAuthority(
                trimmed.startsWith(prefix) ? trimmed : prefix + trimmed
        ));
    }
}
