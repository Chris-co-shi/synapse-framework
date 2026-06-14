package com.indigo.synapse.cache.lock;

import com.indigo.synapse.cache.script.RedisLuaScript;
import com.indigo.synapse.cache.script.RedisScriptExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisReentrantLockTest {

    @Test
    void shouldAcquireLock() {
        CapturingExecutor executor = new CapturingExecutor(1L);
        RedisReentrantLock lock = new RedisReentrantLock(executor);

        LockAcquireResult result = lock.acquire("synapse:cache:test:lock:1", "owner-1", Duration.ofSeconds(30));

        assertTrue(result.acquired());
        assertFalse(result.reentered());
        assertEquals("synapse-reentrant-lock", executor.scriptName);
        assertEquals(List.of("synapse:cache:test:lock:1"), executor.keys);
        assertEquals(List.of("owner-1", "30000"), executor.args);
    }

    @Test
    void shouldDetectReentrantAcquire() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(2L));

        LockAcquireResult result = lock.acquire("lock-key", "owner-1", Duration.ofMillis(100));

        assertTrue(result.acquired());
        assertTrue(result.reentered());
    }

    @Test
    void shouldRejectAcquireWhenOwnedByAnotherOwner() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(0L));

        LockAcquireResult result = lock.acquire("lock-key", "owner-1", Duration.ofMillis(100));

        assertFalse(result.acquired());
        assertFalse(result.reentered());
    }

    @Test
    void shouldReleaseLock() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(0L));

        LockReleaseResult result = lock.release("lock-key", "owner-1", Duration.ofSeconds(1));

        assertTrue(result.released());
        assertTrue(result.ownerMatched());
        assertEquals(0, result.remainingHoldCount());
    }

    @Test
    void shouldKeepLockWhenReleaseStillHasHoldCount() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(1L));

        LockReleaseResult result = lock.release("lock-key", "owner-1", Duration.ofSeconds(1));

        assertFalse(result.released());
        assertTrue(result.ownerMatched());
        assertEquals(1, result.remainingHoldCount());
    }

    @Test
    void shouldRejectReleaseFromAnotherOwner() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(-1L));

        LockReleaseResult result = lock.release("lock-key", "owner-2", Duration.ofSeconds(1));

        assertFalse(result.released());
        assertFalse(result.ownerMatched());
    }

    @Test
    void shouldValidateLockInput() {
        RedisReentrantLock lock = new RedisReentrantLock(new CapturingExecutor(1L));

        assertThrows(IllegalArgumentException.class, () -> new RedisReentrantLock(null));
        assertThrows(IllegalArgumentException.class, () -> lock.acquire("", "owner", Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> lock.acquire("key", "", Duration.ofSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> lock.acquire("key", "owner", Duration.ZERO));
    }

    private static final class CapturingExecutor implements RedisScriptExecutor {

        private final Long result;
        private String scriptName;
        private List<String> keys;
        private List<String> args;

        private CapturingExecutor(Long result) {
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
