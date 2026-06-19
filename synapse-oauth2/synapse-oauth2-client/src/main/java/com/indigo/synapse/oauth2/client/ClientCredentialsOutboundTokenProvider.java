package com.indigo.synapse.oauth2.client;

import java.util.Objects;
import java.util.Optional;

/** 使用 token manager 为出站调用提供 client credentials token。 */
public final class ClientCredentialsOutboundTokenProvider implements OutboundBearerTokenProvider {

    private final OAuth2ClientTokenManager tokenManager;

    /** @param tokenManager token 生命周期管理器 */
    public ClientCredentialsOutboundTokenProvider(OAuth2ClientTokenManager tokenManager) {
        this.tokenManager = Objects.requireNonNull(tokenManager, "tokenManager must not be null");
    }

    @Override
    public Optional<String> token(String registrationId) {
        return Optional.of(tokenManager.getToken(registrationId).value());
    }
}
