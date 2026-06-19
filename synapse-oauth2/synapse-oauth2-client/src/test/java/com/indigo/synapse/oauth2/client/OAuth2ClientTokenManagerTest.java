package com.indigo.synapse.oauth2.client;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2ClientTokenManagerTest {

    private static final Instant NOW = Instant.parse("2026-06-19T00:00:00Z");

    @Test
    void shouldCacheAndRefreshClientCredentialsToken() {
        AtomicInteger acquisitions = new AtomicInteger();
        OAuth2ClientTokenManager manager = new OAuth2ClientTokenManager(
                new InMemoryAuthorizedClientTokenStore(),
                registrationId -> token("token-" + acquisitions.incrementAndGet(), Duration.ofMinutes(10)),
                Clock.fixed(NOW, ZoneOffset.UTC),
                Duration.ofSeconds(30));

        assertThat(manager.getToken("billing").value()).isEqualTo("token-1");
        assertThat(manager.getToken("billing").value()).isEqualTo("token-1");
        manager.invalidate("billing");
        assertThat(manager.getToken("billing").value()).isEqualTo("token-2");
    }

    @Test
    void shouldRefreshTokenInsideSkewWindow() {
        InMemoryAuthorizedClientTokenStore store = new InMemoryAuthorizedClientTokenStore();
        store.save("billing", token("old", Duration.ofSeconds(20)));
        OAuth2ClientTokenManager manager = new OAuth2ClientTokenManager(
                store, id -> token("new", Duration.ofMinutes(5)),
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofSeconds(30));

        assertThat(manager.getToken("billing").value()).isEqualTo("new");
    }

    @Test
    void shouldRelayInboundTokenWithoutChangingPrincipalContext() {
        TokenRelayProvider relay = new TokenRelayProvider(() -> java.util.Optional.of("inbound-token"));

        assertThat(relay.token("ignored")).contains("inbound-token");
    }

    private static OAuth2ClientToken token(String value, Duration lifetime) {
        return new OAuth2ClientToken(value, "Bearer", NOW, NOW.plus(lifetime));
    }
}
