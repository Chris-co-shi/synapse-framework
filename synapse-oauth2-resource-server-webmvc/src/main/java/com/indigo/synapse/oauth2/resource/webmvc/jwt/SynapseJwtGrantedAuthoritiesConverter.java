package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.JwtClaimValues;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Synapse JWT authority 转换器。
 *
 * <p>JWT 字符串集合 claim 的空白过滤、去重和顺序保持统一委托给 OAuth2 Core，
 * 避免 Servlet 与 Reactive Resource Server 产生不同的协议解释。</p>
 */
public final class SynapseJwtGrantedAuthoritiesConverter {

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        JwtClaimAccessor claims = new SpringJwtClaimAccessor(jwt);
        List<GrantedAuthority> authorities = new ArrayList<>();
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.SCOPE)
                .forEach(scope -> add(authorities, "SCOPE_", scope));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.ROLES)
                .forEach(role -> add(authorities, "ROLE_", role));
        JwtClaimValues.strings(claims, SynapseJwtClaimNames.PERMISSIONS)
                .forEach(permission -> add(authorities, "PERM_", permission));
        return List.copyOf(authorities);
    }

    private static void add(List<GrantedAuthority> authorities, String prefix, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String trimmed = value.trim();
        authorities.add(new SimpleGrantedAuthority(
                trimmed.startsWith(prefix) ? trimmed : prefix + trimmed
        ));
    }
}
