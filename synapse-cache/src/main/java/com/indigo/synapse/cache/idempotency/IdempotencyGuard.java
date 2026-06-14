package com.indigo.synapse.cache.idempotency;

import java.time.Duration;

/**
 * 幂等占位保护端口。
 *
 * <p>该接口只表达“在某个 scope 内尝试占用一个幂等 key”的原子能力。scope 和 idempotencyKey 的业务含义
 * 由消费方决定，framework 不定义订单号、请求号、流程号等业务语义。</p>
 */
public interface IdempotencyGuard {

    /**
     * 尝试占用幂等 key。
     *
     * @param scope 幂等范围，例如某个资源、接口或操作域
     * @param idempotencyKey 调用方提供的幂等标识
     * @param ttl 幂等标记有效期
     * @return 首次占用成功返回 true；已存在时返回 false
     */
    boolean tryAcquire(String scope, String idempotencyKey, Duration ttl);
}
