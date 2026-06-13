package com.indigo.synapse.cache.lock;

import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.cache.script.SynapseRedisScripts;

import java.time.Duration;
import java.util.List;

/**
 * Redis Lua 可重入锁。
 *
 * <p>锁数据使用 Redis Hash 保存 owner 与重入次数。释放锁时必须传入同一个 owner，
 * 非 owner 释放会被 Lua 脚本拒绝。</p>
 */
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
