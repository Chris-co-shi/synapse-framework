package com.indigo.synapse.oauth2.jwt;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public record JwtClaims(
        String issuer,
        String subject,
        Set<String> audience,
        String tokenId,
        JwtTokenType tokenType,
        Instant issuedAt,
        Instant expiresAt
) {

    public JwtClaims {
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
        if (issuedAt == null) {
            throw new IllegalArgumentException("issuedAt must not be null");
        }
        if (expiresAt == null || !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        audience = audience == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(audience));
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return !now.isBefore(expiresAt);
    }
}
