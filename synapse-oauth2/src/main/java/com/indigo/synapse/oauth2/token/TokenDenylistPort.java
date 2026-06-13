package com.indigo.synapse.oauth2.token;

import java.time.Instant;

public interface TokenDenylistPort {

    void deny(String tokenId, Instant expiresAt);

    boolean isDenied(String tokenId);
}
