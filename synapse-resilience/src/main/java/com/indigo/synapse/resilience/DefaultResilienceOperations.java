package com.indigo.synapse.resilience;

import com.indigo.synapse.observability.SynapseObservationNames;
import com.indigo.synapse.observability.SynapseObservationOperations;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Resilience4j 默认编排实现。
 *
 * <p>同名策略首次使用时固定配置以保留熔断状态。后续传入同名不同配置会明确失败，避免静默
 * 使用旧策略。该实现不捕获异常生成 fallback，也不会把非幂等操作提升为可重试。</p>
 */
public final class DefaultResilienceOperations implements ResilienceOperations {

    private final ResilienceExceptionClassifier classifier;
    private final SynapseObservationOperations observations;
    private final ExecutorService executor;
    private final ConcurrentMap<String, ResiliencePolicy> policies = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Retry> retries = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Bulkhead> bulkheads = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, TimeLimiter> timeLimiters = new ConcurrentHashMap<>();

    public DefaultResilienceOperations(ResilienceExceptionClassifier classifier,
                                       SynapseObservationOperations observations,
                                       ExecutorService executor) {
        this.classifier = Objects.requireNonNull(classifier, "classifier must not be null");
        this.observations = Objects.requireNonNull(observations, "observations must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    @Override
    public <T> T execute(ResiliencePolicy policy, Callable<T> action) throws Exception {
        Objects.requireNonNull(policy, "policy must not be null");
        Objects.requireNonNull(action, "action must not be null");
        registerPolicy(policy);
        return observations.observe(SynapseObservationNames.RESILIENCE, "resilience", policy.name(), () -> {
            Callable<T> decorated = Bulkhead.decorateCallable(bulkhead(policy), action);
            decorated = CircuitBreaker.decorateCallable(circuitBreaker(policy), decorated);
            decorated = Retry.decorateCallable(retry(policy), decorated);
            Callable<T> finalDecorated = decorated;
            try {
                return timeLimiter(policy).executeFutureSupplier(() -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return finalDecorated.call();
                    } catch (Exception ex) {
                        throw new CompletionException(ex);
                    }
                }, executor));
            } catch (ExecutionException | CompletionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Exception exception) {
                    throw exception;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw ex;
            }
        });
    }

    private void registerPolicy(ResiliencePolicy policy) {
        ResiliencePolicy existing = policies.putIfAbsent(policy.name(), policy);
        if (existing != null && !existing.equals(policy)) {
            throw new ResiliencePolicyConflictException(policy.name());
        }
    }

    private Retry retry(ResiliencePolicy policy) {
        return retries.computeIfAbsent(policy.name(), ignored -> Retry.of(policy.name(), RetryConfig.custom()
                .maxAttempts(policy.maxAttempts())
                .retryOnException(classifier::isRetryable)
                .build()));
    }

    private CircuitBreaker circuitBreaker(ResiliencePolicy policy) {
        return circuitBreakers.computeIfAbsent(policy.name(), ignored -> CircuitBreaker.of(policy.name(),
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(policy.failureRateThreshold())
                        .minimumNumberOfCalls(policy.minimumNumberOfCalls())
                        .slidingWindowSize(Math.max(policy.minimumNumberOfCalls(), 10))
                        .waitDurationInOpenState(policy.openStateWaitDuration())
                        .build()));
    }

    private Bulkhead bulkhead(ResiliencePolicy policy) {
        return bulkheads.computeIfAbsent(policy.name(), ignored -> Bulkhead.of(policy.name(), BulkheadConfig.custom()
                .maxConcurrentCalls(policy.maxConcurrentCalls())
                .maxWaitDuration(policy.bulkheadWaitDuration())
                .build()));
    }

    private TimeLimiter timeLimiter(ResiliencePolicy policy) {
        return timeLimiters.computeIfAbsent(policy.name(), ignored -> TimeLimiter.of(policy.name(),
                TimeLimiterConfig.custom().timeoutDuration(policy.timeout()).cancelRunningFuture(true).build()));
    }
}
