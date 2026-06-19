package com.indigo.synapse.oauth2.client;

import com.indigo.synapse.oauth2.core.token.BearerTokenProvider;

import java.util.Objects;
import java.util.Optional;

/** 读取当前已验证入站 Bearer Token 用于 relay；不解析 claim 或修改当前主体上下文。 */
public final class TokenRelayProvider implements OutboundBearerTokenProvider {

    private final BearerTokenProvider bearerTokenProvider;

    /** @param bearerTokenProvider 当前入站 token 读取端口 */
    public TokenRelayProvider(BearerTokenProvider bearerTokenProvider) {
        this.bearerTokenProvider = Objects.requireNonNull(bearerTokenProvider, "bearerTokenProvider must not be null");
    }

    @Override
    public Optional<String> token(String registrationId) {
        return bearerTokenProvider.currentToken().filter(value -> !value.isBlank());
    }
}
