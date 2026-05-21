package com.indigo.synapse.security.jwk;

import java.time.Instant;

public record JwkKeyDescriptor(
        String keyId,
        String algorithm,
        String use,
        Instant createdAt,
        Instant expiresAt
) {

    public static final String SIGNATURE_USE = "sig";

    public JwkKeyDescriptor {
        if (keyId == null || keyId.isBlank()) {
            throw new IllegalArgumentException("keyId must not be blank");
        }
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("algorithm must not be blank");
        }
        if (use == null || use.isBlank()) {
            throw new IllegalArgumentException("use must not be blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (expiresAt != null && !expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("expiresAt must be after createdAt");
        }
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("now must not be null");
        }
        return expiresAt != null && !now.isBefore(expiresAt);
    }
}
