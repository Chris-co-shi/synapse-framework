package com.indigo.synapse.cache.lock;

import com.indigo.synapse.cache.script.RedisScriptExecutor;
import com.indigo.synapse.cache.script.SynapseRedisScripts;

import java.time.Duration;
import java.util.List;

/**
 * Redis Lua 可重入锁。
 *
 * <p>锁数据使用 Redis Hash 保存 owner 与重入次数。释放锁时必须传入同一个 owner，
 * 非 owner 释放会被 Lua 脚本拒绝。该实现只提供 acquire/release 原子能力，不负责业务重试、阻塞等待、
 * 自动续约或 try-with-resources 包装。</p>
 *
 * <p>owner 应由消费方生成稳定且唯一的调用方标识，例如实例 ID + 线程 ID + 请求 ID。</p>
 */
public final class RedisReentrantLock {

    private final RedisScriptExecutor scriptExecutor;

    public RedisReentrantLock(RedisScriptExecutor scriptExecutor) {
        if (scriptExecutor == null) {
            throw new IllegalArgumentException("scriptExecutor must not be null");
        }
        this.scriptExecutor = scriptExecutor;
    }

    /**
     * 尝试获取锁。
     *
     * @param lockKey 锁 key
     * @param owner 锁持有者标识
     * @param leaseTime 租约时间，必须为正数
     * @return 获取结果
     */
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

    /**
     * 释放锁或减少重入次数。
     *
     * @param lockKey 锁 key
     * @param owner 锁持有者标识，必须与加锁时一致
     * @param leaseTime 仍然持有锁时用于刷新剩余租约
     * @return 释放结果
     */
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
