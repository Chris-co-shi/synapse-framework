package com.indigo.synapse.messaging.idempotent;

/**
 * 默认无操作 MQ 幂等检查器。
 *
 * <p>该实现不会提供真实幂等保护：{@link #isProcessed(String)} 永远返回 false，
 * {@link #markProcessed(String)} 不保存任何状态。生产环境如果需要消费幂等，应由业务方或后续适配模块替换。</p>
 */
public final class NoopMessageIdempotencyChecker implements MessageIdempotencyChecker {

    @Override
    public boolean isProcessed(String idempotentKey) {
        return false;
    }

    @Override
    public void markProcessed(String idempotentKey) {
        // no-op by design
    }
}
