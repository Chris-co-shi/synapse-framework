package com.indigo.synapse.oauth2.token;

import java.time.Instant;
import java.util.Objects;

/**
 * 不做实际存储的 token denylist 实现。
 *
 * <p>该实现只做参数校验，永远认为 token 未被拒绝。它仅适合开发和测试环境；生产环境必须提供真实实现，
 * 例如 Redis、数据库或集中式 token 状态服务。</p>
 */
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
