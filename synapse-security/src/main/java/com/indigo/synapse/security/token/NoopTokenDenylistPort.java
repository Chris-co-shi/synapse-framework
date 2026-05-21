package com.indigo.synapse.security.token;

import java.time.Instant;
import java.util.Objects;

public final class NoopTokenDenylistPort implements TokenDenylistPort {

    @Override
    public void deny(String tokenId, Instant expiresAt) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("tokenId must not be blank");
        }
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }

    @Override
    public boolean isDenied(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("tokenId must not be blank");
        }
        return false;
    }
}
