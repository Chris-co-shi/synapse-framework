package com.indigo.synapse.oauth2.client;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;

/**
 * 编排 client credentials token 的读取、提前刷新与存储。
 *
 * <p>该类型不会绑定 CurrentPrincipalContext，机器身份不会污染当前入站用户身份。</p>
 */
public final class OAuth2ClientTokenManager {

    private final AuthorizedClientTokenStore tokenStore;
    private final ClientCredentialsTokenProvider tokenProvider;
    private final Clock clock;
    private final Duration refreshSkew;

    /**
     * @param tokenStore authorized-client token 存储
     * @param tokenProvider client credentials 获取端口
     * @param clock 生命周期判断时钟
     * @param refreshSkew 提前刷新窗口，null 时为 30 秒
     */
    public OAuth2ClientTokenManager(AuthorizedClientTokenStore tokenStore,
                                    ClientCredentialsTokenProvider tokenProvider,
                                    Clock clock,
                                    Duration refreshSkew) {
        this.tokenStore = Objects.requireNonNull(tokenStore, "tokenStore must not be null");
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.refreshSkew = refreshSkew == null ? Duration.ofSeconds(30) : refreshSkew;
        if (this.refreshSkew.isNegative()) {
            throw new IllegalArgumentException("refreshSkew must not be negative");
        }
    }

    /**
     * @param registrationId 客户端注册标识
     * @return 可用 token；缺失或进入刷新窗口时重新获取
     */
    public synchronized OAuth2ClientToken getToken(String registrationId) {
        return tokenStore.load(registrationId)
                .filter(token -> !token.requiresRefresh(clock.instant(), refreshSkew))
                .orElseGet(() -> acquireAndSave(registrationId));
    }

    /** @param registrationId 要失效的客户端注册标识 */
    public void invalidate(String registrationId) {
        tokenStore.remove(registrationId);
    }

    private OAuth2ClientToken acquireAndSave(String registrationId) {
        OAuth2ClientToken token = Objects.requireNonNull(tokenProvider.acquire(registrationId),
                "tokenProvider returned null");
        tokenStore.save(registrationId, token);
        return token;
    }
}
