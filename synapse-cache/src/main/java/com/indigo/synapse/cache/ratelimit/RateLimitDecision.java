package com.indigo.synapse.cache.ratelimit;

/**
 * 滑动窗口限流决策结果。
 *
 * @param allowed 当前请求是否允许通过
 * @param limit 窗口内最大允许次数
 * @param remaining 当前窗口剩余可用次数
 * @param resetAtMillis 当前窗口预计恢复时间，epoch millis
 */
public record RateLimitDecision(boolean allowed, long limit, long remaining, long resetAtMillis) {

    public RateLimitDecision {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be positive");
        }
        if (remaining < 0) {
            throw new IllegalArgumentException("remaining must not be negative");
        }
        if (resetAtMillis < 0) {
            throw new IllegalArgumentException("resetAtMillis must not be negative");
        }
    }
}
