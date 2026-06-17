package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

/**
 * 携带 Synapse AuthenticatedPrincipal 的 JWT Authentication。
 */
public final class SynapseJwtAuthenticationToken extends JwtAuthenticationToken {

    private final AuthenticatedPrincipal principal;
    private final TokenMetadata tokenMetadata;

    public SynapseJwtAuthenticationToken(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            AuthenticatedPrincipal principal,
            TokenMetadata tokenMetadata) {
        super(jwt, authorities, principal.principalId());
        this.principal = principal;
        this.tokenMetadata = tokenMetadata;
    }

    public AuthenticatedPrincipal authenticatedPrincipal() {
        return principal;
    }

    public TokenMetadata tokenMetadata() {
        return tokenMetadata;
    }
}
