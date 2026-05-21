package com.indigo.synapse.cache.ratelimit;

import com.indigo.synapse.cache.script.RedisLuaScript;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowRateLimiterTest {

    @Test
    void shouldAllowRequestAndExposeRemainingQuota() {
        CapturingExecutor executor = new CapturingExecutor(List.of(1L, 4L, 11000L));
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(executor);

        RateLimitDecision decision = limiter.allow("synapse:web:login:rate:user-1", 5, Duration.ofSeconds(10), 1000L);

        assertTrue(decision.allowed());
        assertEquals(5, decision.limit());
        assertEquals(4, decision.remaining());
        assertEquals(11000L, decision.resetAtMillis());
        assertEquals("synapse-sliding-window-rate-limit", executor.scriptName);
        assertEquals(List.of("synapse:web:login:rate:user-1"), executor.keys);
        assertEquals("1000", executor.args.get(0));
        assertEquals("10000", executor.args.get(1));
        assertEquals("5", executor.args.get(2));
    }

    @Test
    void shouldDenyRequestWhenLimitExceeded() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(new CapturingExecutor(List.of(0L, 0L, 5000L)));

        RateLimitDecision decision = limiter.allow("rate-key", 3, Duration.ofSeconds(1), 1000L);

        assertFalse(decision.allowed());
        assertEquals(0, decision.remaining());
        assertEquals(5000L, decision.resetAtMillis());
    }

    @Test
    void shouldValidateInput() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(new CapturingExecutor(List.of(1L, 1L, 1L)));

        assertThrows(IllegalArgumentException.class, () -> new SlidingWindowRateLimiter(null));
        assertThrows(IllegalArgumentException.class, () -> limiter.allow("", 1, Duration.ofSeconds(1), 1L));
        assertThrows(IllegalArgumentException.class, () -> limiter.allow("key", 0, Duration.ofSeconds(1), 1L));
        assertThrows(IllegalArgumentException.class, () -> limiter.allow("key", 1, Duration.ZERO, 1L));
        assertThrows(IllegalArgumentException.class, () -> limiter.allow("key", 1, Duration.ofSeconds(1), -1L));
    }

    @Test
    void shouldRejectMalformedScriptResult() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(new CapturingExecutor(List.of(1L, 1L)));

        assertThrows(IllegalArgumentException.class, () -> limiter.allow("key", 1, Duration.ofSeconds(1), 1L));
    }

    private static final class CapturingExecutor implements RedisScriptExecutor {

        private final List<?> result;
        private String scriptName;
        private List<String> keys;
        private List<String> args;

        private CapturingExecutor(List<?> result) {
            this.result = result;
        }

        @Override
        public <T> T execute(RedisLuaScript<T> script, List<String> keys, List<String> args) {
            this.scriptName = script.name();
            this.keys = keys;
            this.args = args;
            return script.resultType().cast(result);
        }
    }
}
