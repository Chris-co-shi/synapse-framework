package com.indigo.synapse.resilience;

import com.indigo.synapse.observability.DefaultSynapseObservationOperations;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultResilienceOperationsTest {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final DefaultResilienceOperations operations = new DefaultResilienceOperations(
            new DefaultResilienceExceptionClassifier(),
            new DefaultSynapseObservationOperations(ObservationRegistry.NOOP), executor);

    @AfterEach
    void closeExecutor() {
        executor.close();
    }

    @Test
    void shouldRejectRetryForNonIdempotentOperation() {
        assertThatThrownBy(() -> policy("payment", false, 2, Duration.ofSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-idempotent");
    }

    @Test
    void shouldRetryOnlyClassifiedFailureForIdempotentOperation() throws Exception {
        AtomicInteger attempts = new AtomicInteger();

        String result = operations.execute(policy("inventory-read", true, 3, Duration.ofSeconds(2)), () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IOException("temporary");
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(attempts).hasValue(3);
    }

    @Test
    void shouldNotRetryUnclassifiedBusinessFailure() {
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> operations.execute(
                policy("order-create", true, 3, Duration.ofSeconds(2)), () -> {
                    attempts.incrementAndGet();
                    throw new IllegalArgumentException("invalid order");
                }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid order");
        assertThat(attempts).hasValue(1);
    }

    @Test
    void shouldEnforceTimeoutWithoutFallback() {
        assertThatThrownBy(() -> operations.execute(
                policy("slow-read", true, 1, Duration.ofMillis(20)), () -> {
                    Thread.sleep(200);
                    return "late";
                }))
                .isInstanceOf(TimeoutException.class);
    }

    @Test
    void shouldOpenCircuitAfterConfiguredFailures() {
        AtomicInteger calls = new AtomicInteger();
        ResiliencePolicy policy = new ResiliencePolicy("unstable-service", true, 1, Duration.ofSeconds(1),
                50, 2, Duration.ofSeconds(10), 10, Duration.ZERO);

        assertThatThrownBy(() -> operations.execute(policy, () -> {
            calls.incrementAndGet();
            throw new IOException("down");
        })).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> operations.execute(policy, () -> {
            calls.incrementAndGet();
            throw new IOException("down");
        })).isInstanceOf(IOException.class);
        assertThatThrownBy(() -> operations.execute(policy, () -> {
            calls.incrementAndGet();
            return "unexpected";
        })).isInstanceOf(io.github.resilience4j.circuitbreaker.CallNotPermittedException.class);
        assertThat(calls).hasValue(2);
    }

    @Test
    void shouldRejectConcurrentCallWhenBulkheadIsFull() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ResiliencePolicy policy = new ResiliencePolicy("single-flight", true, 1, Duration.ofSeconds(2),
                100, 10, Duration.ofSeconds(1), 1, Duration.ZERO);
        CompletableFuture<Void> first = CompletableFuture.runAsync(() -> {
            try {
                operations.execute(policy, () -> {
                    entered.countDown();
                    release.await(1, TimeUnit.SECONDS);
                    return null;
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        assertThat(entered.await(1, TimeUnit.SECONDS)).isTrue();

        try {
            assertThatThrownBy(() -> operations.execute(policy, () -> null))
                    .isInstanceOf(io.github.resilience4j.bulkhead.BulkheadFullException.class);
        } finally {
            release.countDown();
            first.join();
        }
    }

    @Test
    void shouldRejectDifferentConfigurationForExistingPolicyName() throws Exception {
        ResiliencePolicy first = policy("inventory-client", true, 1, Duration.ofSeconds(1));
        ResiliencePolicy conflicting = policy("inventory-client", true, 2, Duration.ofSeconds(2));

        assertThat(operations.execute(first, () -> "ok")).isEqualTo("ok");
        assertThatThrownBy(() -> operations.execute(conflicting, () -> "unexpected"))
                .isInstanceOf(ResiliencePolicyConflictException.class)
                .hasMessageContaining("inventory-client");
    }

    private static ResiliencePolicy policy(String name, boolean idempotent, int attempts, Duration timeout) {
        return new ResiliencePolicy(name, idempotent, attempts, timeout,
                50, 10, Duration.ofSeconds(1), 10, Duration.ZERO);
    }
}
