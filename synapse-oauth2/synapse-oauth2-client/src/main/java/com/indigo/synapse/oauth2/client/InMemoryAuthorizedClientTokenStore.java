package com.indigo.synapse.oauth2.client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** 线程安全的进程内 token 存储，仅适合单实例或测试，不提供持久化和集群一致性。 */
public final class InMemoryAuthorizedClientTokenStore implements AuthorizedClientTokenStore {

    private final ConcurrentMap<String, OAuth2ClientToken> tokens = new ConcurrentHashMap<>();

    @Override
    public Optional<OAuth2ClientToken> load(String registrationId) {
        return Optional.ofNullable(tokens.get(requireRegistrationId(registrationId)));
    }

    @Override
    public void save(String registrationId, OAuth2ClientToken token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null");
        }
        tokens.put(requireRegistrationId(registrationId), token);
    }

    @Override
    public void remove(String registrationId) {
        tokens.remove(requireRegistrationId(registrationId));
    }

    private static String requireRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.isBlank()) {
            throw new IllegalArgumentException("registrationId must not be blank");
        }
        return registrationId;
    }
}
