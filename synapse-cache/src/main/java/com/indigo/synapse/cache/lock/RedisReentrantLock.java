package com.indigo.synapse.cache.lock;

import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.cache.script.SynapseRedisScripts;

import java.time.Duration;
import java.util.List;

public final class RedisReentrantLock {

    private final RedisScriptExecutor scriptExecutor;

    public RedisReentrantLock(RedisScriptExecutor scriptExecutor) {
        if (scriptExecutor == null) {
            throw new IllegalArgumentException("scriptExecutor must not be null");
        }
        this.scriptExecutor = scriptExecutor;
    }

    public LockAcquireResult acquire(String lockKey, String owner, Duration leaseTime) {
        validateKeyAndOwner(lockKey, owner);
        long leaseMillis = validateLeaseTime(leaseTime);
        Long result = scriptExecutor.execute(
                SynapseRedisScripts.REENTRANT_LOCK,
                List.of(lockKey),
                List.of(owner, Long.toString(leaseMillis))
        );
        return LockAcquireResult.fromScriptResult(result);
    }

    public LockReleaseResult release(String lockKey, String owner, Duration leaseTime) {
        validateKeyAndOwner(lockKey, owner);
        long leaseMillis = validateLeaseTime(leaseTime);
        Long result = scriptExecutor.execute(
                SynapseRedisScripts.REENTRANT_UNLOCK,
                List.of(lockKey),
                List.of(owner, Long.toString(leaseMillis))
        );
        return LockReleaseResult.fromScriptResult(result);
    }

    private static void validateKeyAndOwner(String lockKey, String owner) {
        if (lockKey == null || lockKey.isBlank()) {
            throw new IllegalArgumentException("lockKey must not be blank");
        }
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("owner must not be blank");
        }
    }

    private static long validateLeaseTime(Duration leaseTime) {
        if (leaseTime == null || leaseTime.isZero() || leaseTime.isNegative()) {
            throw new IllegalArgumentException("leaseTime must be positive");
        }
        return leaseTime.toMillis();
    }
}
