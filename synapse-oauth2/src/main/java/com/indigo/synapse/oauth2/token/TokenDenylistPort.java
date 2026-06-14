package com.indigo.synapse.oauth2.token;

import java.time.Instant;

/**
 * token 拒绝列表端口。
 *
 * <p>该端口用于表达 token 主动失效、退出登录、强制下线等场景所需的 denylist 能力。oauth2 模块只定义端口，
 * 不绑定 Redis、数据库或其他持久化实现。</p>
 */
public interface TokenDenylistPort {

    /**
     * 将 tokenId 加入拒绝列表。
     *
     * @param tokenId token 唯一标识，通常对应 JWT jti
     * @param expiresAt token 原始过期时间，存储实现可用于设置过期时间
     */
    void deny(String tokenId, Instant expiresAt);

    /**
     * 判断 tokenId 是否已被拒绝。
     *
     * @param tokenId token 唯一标识
     * @return 已在拒绝列表中时返回 true
     */
    boolean isDenied(String tokenId);
}
