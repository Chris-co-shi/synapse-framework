package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 操作上下文读取端口。
 *
 * <p>该接口是其他模块读取当前操作上下文的稳定入口。data、audit、mq、file 等模块应依赖该接口，
 * 而不是直接依赖 security、web、ThreadLocal 或某个业务用户模型。这样可以保证 HTTP、MQ、Task、Async
 * 等不同入口都能用同一套上下文机制。</p>
 *
 * <p>消费方可以替换该接口实现，例如从自定义上下文容器读取；默认实现基于 {@link OperationContextHolder}。</p>
 */
public interface OperationContextProvider {

    /**
     * 返回当前操作上下文。
     *
     * @return 当前上下文；没有上下文时返回 empty
     */
    Optional<OperationContext> current();

    /**
     * 返回当前操作人的稳定标识。
     *
     * @return actor id；没有上下文、没有 actor 或 id 为空时返回 empty
     */
    default Optional<String> currentActorId() {
        return current().map(OperationContext::actor)
                .map(OperationActor::id)
                .filter(value -> !value.isBlank());
    }

    /**
     * 返回当前租户标识。
     *
     * <p>当前不实现租户隔离或数据范围规则，只保留跨模块上下文承载位。</p>
     *
     * @return tenant id；没有上下文或值为空时返回 empty
     */
    default Optional<String> currentTenantId() {
        return current().map(OperationContext::tenantId)
                .filter(value -> !value.isBlank());
    }

    /**
     * 返回当前追踪标识。
     *
     * @return trace id；没有上下文或值为空时返回 empty
     */
    default Optional<String> currentTraceId() {
        return current().map(OperationContext::traceId)
                .filter(value -> !value.isBlank());
    }
}
