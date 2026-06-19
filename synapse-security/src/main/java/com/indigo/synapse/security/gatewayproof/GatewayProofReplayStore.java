package com.indigo.synapse.security.gatewayproof;

import java.time.Duration;

/**
 * GatewayProof nonce 重放保护存储端口。
 *
 * <p>该接口 Web 无关，不绑定 Redis 或其他存储实现。生产多实例环境必须提供分布式实现；
 * Framework 不默认启用无效的 no-op 实现。调用方必须在签名成功后再写入，避免失败请求污染 nonce 集合。</p>
 */
public interface GatewayProofReplayStore {

    /**
     * 仅当指定 Gateway 与 nonce 组合不存在时写入。
     *
     * @param gatewayId Gateway 标识
     * @param nonce 一次性随机值
     * @param ttl 存活时间
     * @return 第一次出现返回 true，已存在返回 false
     */
    boolean markIfAbsent(String gatewayId, String nonce, Duration ttl);
}
