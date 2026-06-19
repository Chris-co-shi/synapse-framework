package com.indigo.synapse.oauth2.client;

import java.util.Optional;

/** Authorized Client token 存储端口；Framework 不提供数据库或 Redis 实现。 */
public interface AuthorizedClientTokenStore {

    /**
     * @param registrationId 客户端注册标识
     * @return 已保存的 token，缺失时为空
     */
    Optional<OAuth2ClientToken> load(String registrationId);

    /**
     * @param registrationId 客户端注册标识
     * @param token 待保存的敏感 token
     */
    void save(String registrationId, OAuth2ClientToken token);

    /** @param registrationId 客户端注册标识 */
    void remove(String registrationId);
}
