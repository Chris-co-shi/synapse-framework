package com.indigo.synapse.oauth2.authorization.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;

/**
 * Synapse JWT 签发器。
 */
public final class SynapseJwtIssuer {

    private final JwtEncoder jwtEncoder;
    private final String keyId;

    public SynapseJwtIssuer(JwtEncoder jwtEncoder, String keyId) {
        if (jwtEncoder == null) {
            throw new IllegalArgumentException("jwtEncoder must not be null");
        }
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }
        this.jwtEncoder = jwtEncoder;
        this.keyId = keyId;
    }

    public String issue(JwtIssuanceClaims claims) {
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(claims.issuer())
                .subject(claims.subject())
                .id(claims.tokenId())
                .issuedAt(claims.issuedAt())
                .expiresAt(claims.expiresAt())
                .claim(SynapseJwtClaimNames.TOKEN_TYPE, claims.tokenType().name())
                .claim(SynapseJwtClaimNames.PRINCIPAL_TYPE, claims.principalType());
        if (!claims.audience().isEmpty()) {
            builder.audience(claims.audience().stream().toList());
        }
        claims.additionalClaims().forEach(builder::claim);
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(keyId).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, builder.build())).getTokenValue();
    }
}
