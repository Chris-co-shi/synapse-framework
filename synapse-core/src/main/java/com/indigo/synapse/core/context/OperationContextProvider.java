package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 操作上下文读取端口。
 *
 * <p>其他模块应优先依赖该接口读取 actor、tenant 和 trace，而不是直接依赖具体安全上下文。</p>
 */
public interface OperationContextProvider {

    Optional<OperationContext> current();

    default Optional<String> currentActorId() {
        return current().map(OperationContext::actor)
                .map(OperationActor::id)
                .filter(value -> !value.isBlank());
    }

    default Optional<String> currentTenantId() {
        return current().map(OperationContext::tenantId)
                .filter(value -> !value.isBlank());
    }

    default Optional<String> currentTraceId() {
        return current().map(OperationContext::traceId)
                .filter(value -> !value.isBlank());
    }
}
