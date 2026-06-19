package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import com.indigo.synapse.oauth2.resource.core.SynapseAuthorityClaimMapper;
import com.indigo.synapse.oauth2.resource.core.SynapsePrincipalClaimMapper;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * Reactive JWT 到 Authentication 转换器。
 *
 * <p>JWT claim 的必填校验和字符串集合规范化统一委托给 OAuth2 Core，
 * 避免 Servlet 与 Reactive Resource Server 产生不同的协议解释。</p>
 */
public final class SynapseReactiveJwtAuthenticationConverter
        implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final SynapsePrincipalClaimMapper principalMapper = new SynapsePrincipalClaimMapper();
    private final SynapseAuthorityClaimMapper authorityMapper = new SynapseAuthorityClaimMapper();

    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt jwt) {
        JwtClaimAccessor claims = new SpringJwtClaimAccessor(jwt);
        AuthenticatedPrincipal principal = principalMapper.map(claims);
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                jwt,
                authorities(claims),
                principal.principalId()
        );
        authentication.setDetails(principal);
        return Mono.just(authentication);
    }

    private Collection<SimpleGrantedAuthority> authorities(JwtClaimAccessor claims) {
        return authorityMapper.map(claims).stream().map(SimpleGrantedAuthority::new).toList();
    }
}
