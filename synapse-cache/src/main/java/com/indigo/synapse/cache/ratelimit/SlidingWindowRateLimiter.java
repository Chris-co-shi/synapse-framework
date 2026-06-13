package com.indigo.synapse.cache.ratelimit;

import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.cache.script.SynapseRedisScripts;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Redis Lua 滑动窗口限流器。
 *
 * <p>窗口数据使用 ZSET 保存请求时间戳。该实现依赖调用方传入当前毫秒时间，
 * 多实例部署时建议使用统一时钟来源，避免节点时钟漂移影响限流精度。</p>
 */
public final class SlidingWindowRateLimiter {

    private final RedisScriptExecutor scriptExecutor;

    public SlidingWindowRateLimiter(RedisScriptExecutor scriptExecutor) {
        if (scriptExecutor == null) {
            throw new IllegalArgumentException("scriptExecutor must not be null");
        }
        this.scriptExecutor = scriptExecutor;
    }

    public RateLimitDecision allow(String key, long limit, Duration window, long nowMillis) {
        validate(key, limit, window, nowMillis);
        List<?> result = scriptExecutor.execute(
                SynapseRedisScripts.SLIDING_WINDOW_RATE_LIMIT,
                List.of(key),
                List.of(
                        Long.toString(nowMillis),
                        Long.toString(window.toMillis()),
                        Long.toString(limit),
                        nowMillis + "-" + UUID.randomUUID()
                )
        );
        return toDecision(result, limit);
    }

    private static RateLimitDecision toDecision(List<?> result, long limit) {
        if (result == null || result.size() != 3) {
            throw new IllegalArgumentException("rate limit script result must contain allowed, remaining, resetAtMillis");
        }
        long allowed = toLong(result.get(0), "allowed");
        long remaining = toLong(result.get(1), "remaining");
        long resetAtMillis = toLong(result.get(2), "resetAtMillis");
        return new RateLimitDecision(allowed == 1, limit, remaining, resetAtMillis);
    }

    private static void validate(String key, long limit, Duration window, long nowMillis) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (window == null || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("window must be positive");
        }
        if (nowMillis < 0) {
            throw new IllegalArgumentException("nowMillis must not be negative");
        }
    }

    private static long toLong(Object value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        throw new IllegalArgumentException(name + " must be numeric");
    }
}
