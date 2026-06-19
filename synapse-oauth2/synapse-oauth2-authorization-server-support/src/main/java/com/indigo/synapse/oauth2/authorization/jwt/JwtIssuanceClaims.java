package com.indigo.synapse.oauth2.authorization.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * JWT 签发 claims。
 */
public record JwtIssuanceClaims(
        String issuer,
        String subject,
        Set<String> audience,
        String tokenId,
        SynapseTokenType tokenType,
        String principalType,
        Instant issuedAt,
        Instant expiresAt,
        Map<String, Object> additionalClaims
) {

    public JwtIssuanceClaims {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException("issuer must not be blank");
        }
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("tokenId must not be blank");
        }
        if (tokenType == null) {
            throw new IllegalArgumentException("tokenType must not be null");
        }
        if (principalType == null || principalType.isBlank()) {
            throw new IllegalArgumentException("principalType must not be blank");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }
        if (expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        audience = audience == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(audience));
        additionalClaims = additionalClaims == null ? Map.of() : Map.copyOf(additionalClaims);
    }
}
