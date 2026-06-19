package com.indigo.synapse.mybatisplus.audit;

import com.indigo.synapse.core.context.OperationActor;
import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContextProvider;
import com.indigo.synapse.data.audit.DataAuditorProvider;

import java.util.Optional;

/**
 * 基于 OperationContext 的数据审计人读取适配器。
 *
 * <p>该适配器读取当前 actor id，用于 MyBatis-Plus 自动填充 createdBy / updatedBy。
 * 它不读取 tenantId，也不实现租户隔离或数据权限规则。显式 SYSTEM actor 可以作为审计人；
 * UNKNOWN actor 不会写入数据审计字段。</p>
 */
public final class OperationContextDataAuditorProvider implements DataAuditorProvider {

    private final OperationContextProvider operationContextProvider;

    public OperationContextDataAuditorProvider(OperationContextProvider operationContextProvider) {
        this.operationContextProvider = operationContextProvider;
    }

    @Override
    public Optional<String> currentAuditor() {
        if (operationContextProvider == null) {
            return Optional.empty();
        }
        return operationContextProvider.current()
                .map(context -> context.actor())
                .filter(this::auditable)
                .map(OperationActor::id)
                .filter(value -> !value.isBlank());
    }

    private boolean auditable(OperationActor actor) {
        return actor != null
                && actor.type() != OperationActorType.UNKNOWN
                && actor.id() != null;
    }
}
