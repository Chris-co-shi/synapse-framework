package com.indigo.synapse.oauth2.jwt;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

import java.util.LinkedHashSet;
import java.util.Set;

public class SynapseJwtService {

    public static final String TOKEN_TYPE_CLAIM = "token_type";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String keyId;

    public SynapseJwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, String keyId) {
        if (jwtEncoder == null) {
            throw new IllegalArgumentException("jwtEncoder must not be null");
        }
        if (jwtDecoder == null) {
            throw new IllegalArgumentException("jwtDecoder must not be null");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.keyId = keyId;
    }

    public String issue(JwtClaims claims) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(claims.issuer())
                .subject(claims.subject())
                .id(claims.tokenId())
                .issuedAt(claims.issuedAt())
                .expiresAt(claims.expiresAt())
                .claim(TOKEN_TYPE_CLAIM, claims.tokenType().name());
        if (!claims.audience().isEmpty()) {
            builder.audience(claims.audience().stream().toList());
        }
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(keyId)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
    }

    public JwtClaims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token must not be blank");
        }
        Jwt jwt = jwtDecoder.decode(token);
        String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);
        return new JwtClaims(
                jwt.getClaimAsString("iss"),
                jwt.getSubject(),
                new LinkedHashSet<>(audience(jwt)),
                jwt.getId(),
                JwtTokenType.valueOf(tokenType),
                jwt.getIssuedAt(),
                jwt.getExpiresAt()
        );
    }

    private static Set<String> audience(Jwt jwt) {
        return jwt.getAudience() == null ? Set.of() : new LinkedHashSet<>(jwt.getAudience());
    }
}
