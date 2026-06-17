package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Synapse JWT authority 转换器。
 */
public final class SynapseJwtGrantedAuthoritiesConverter {

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        strings(jwt, SynapseJwtClaimNames.SCOPE).forEach(scope -> add(authorities, "SCOPE_", scope));
        strings(jwt, SynapseJwtClaimNames.ROLES).forEach(role -> add(authorities, "ROLE_", role));
        strings(jwt, SynapseJwtClaimNames.PERMISSIONS).forEach(permission -> add(authorities, "PERM_", permission));
        return List.copyOf(authorities);
    }

    private static void add(List<GrantedAuthority> authorities, String prefix, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String trimmed = value.trim();
        authorities.add(new SimpleGrantedAuthority(trimmed.startsWith(prefix) ? trimmed : prefix + trimmed));
    }

    @SuppressWarnings("unchecked")
    private static Collection<String> strings(Jwt jwt, String claim) {
        Object value = jwt.getClaims().get(claim);
        if (value instanceof String string) {
            return List.of(string.split(" "));
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }
}
