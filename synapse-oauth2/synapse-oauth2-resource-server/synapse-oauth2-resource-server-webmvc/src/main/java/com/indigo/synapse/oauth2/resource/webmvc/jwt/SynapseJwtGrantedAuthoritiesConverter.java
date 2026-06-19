package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.resource.core.SynapseAuthorityClaimMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Synapse JWT authority 转换器。
 *
 * <p>JWT 字符串集合 claim 的空白过滤、去重和顺序保持统一委托给 OAuth2 Core，
 * 避免 Servlet 与 Reactive Resource Server 产生不同的协议解释。</p>
 */
public final class SynapseJwtGrantedAuthoritiesConverter {

    private final SynapseAuthorityClaimMapper delegate = new SynapseAuthorityClaimMapper();

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return delegate.map(new SpringJwtClaimAccessor(jwt)).stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
