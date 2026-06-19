package com.indigo.synapse.oauth2.client;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    void shouldCoordinateRefreshPerRegistration() throws Exception {
        AtomicInteger acquisitions = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        OAuth2ClientTokenManager manager = new OAuth2ClientTokenManager(
                new InMemoryAuthorizedClientTokenStore(),
                id -> {
                    acquisitions.incrementAndGet();
                    entered.countDown();
                    await(release);
                    return token("shared", Duration.ofMinutes(5));
                },
                Clock.fixed(NOW, ZoneOffset.UTC), Duration.ofSeconds(30));

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<OAuth2ClientToken> first = CompletableFuture.supplyAsync(
                    () -> manager.getToken("billing"), executor);
            assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();
            CompletableFuture<OAuth2ClientToken> second = CompletableFuture.supplyAsync(
                    () -> manager.getToken("billing"), executor);
            release.countDown();

            assertThat(first.join().value()).isEqualTo("shared");
            assertThat(second.join().value()).isEqualTo("shared");
            assertThat(acquisitions).hasValue(1);
        }
    }

    @Test
    void shouldNotExposeTokenValueFromToString() {
        OAuth2ClientToken token = token("top-secret-access-token", Duration.ofMinutes(5));

        assertThat(token.toString())
                .contains("value=<redacted>")
                .doesNotContain(token.value());
    }

    @Test
    void shouldRelayInboundTokenWithoutChangingPrincipalContext() {
        TokenRelayProvider relay = new TokenRelayProvider(() -> java.util.Optional.of("inbound-token"));

        assertThat(relay.token("ignored")).contains("inbound-token");
    }

    private static OAuth2ClientToken token(String value, Duration lifetime) {
        return new OAuth2ClientToken(value, "Bearer", NOW, NOW.plus(lifetime));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test latch timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("test interrupted", exception);
        }
    }
}
