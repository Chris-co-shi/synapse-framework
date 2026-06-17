package com.indigo.synapse.oauth2.core.token;

import java.time.Instant;

/**
 * token 拒绝列表端口。
 */
public interface TokenDenylistPort {

    void deny(String tokenId, Instant expiresAt);

    boolean isDenied(String tokenId);
}
