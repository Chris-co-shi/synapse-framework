package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * JWT 到 Spring Authentication 的转换器。
 */
public final class SynapseJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    private final SynapseJwtPrincipalMapper principalMapper;
    private final SynapseJwtGrantedAuthoritiesConverter authoritiesConverter;

    public SynapseJwtAuthenticationConverter() {
        this(new SynapseJwtPrincipalMapper(), new SynapseJwtGrantedAuthoritiesConverter());
    }

    public SynapseJwtAuthenticationConverter(
            SynapseJwtPrincipalMapper principalMapper,
            SynapseJwtGrantedAuthoritiesConverter authoritiesConverter) {
        this.principalMapper = principalMapper;
        this.authoritiesConverter = authoritiesConverter;
    }

    @Override
    public JwtAuthenticationToken convert(Jwt jwt) {
        AuthenticatedPrincipal principal = principalMapper.map(jwt);
        return new SynapseJwtAuthenticationToken(
                jwt,
                authoritiesConverter.convert(jwt),
                principal,
                new TokenMetadata(jwt.getId(), jwt.getClaimAsString(SynapseJwtClaimNames.ISSUER))
        );
    }
}
