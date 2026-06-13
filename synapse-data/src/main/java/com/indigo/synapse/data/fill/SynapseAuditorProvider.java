package com.indigo.synapse.data.fill;

import com.indigo.synapse.core.context.OperationContextProvider;

import java.util.Optional;

@FunctionalInterface
public interface SynapseAuditorProvider {

    Optional<String> currentAuditor();

    default Optional<String> currentTenantId() {
        return Optional.empty();
    }

    static SynapseAuditorProvider empty() {
        return Optional::empty;
    }

    /**
     * 基于通用操作上下文创建数据填充使用的审计信息读取器。
     *
     * <p>该接口当前仍服务于 data fill，后续可被更明确的 OperationContext 方向 provider 替代。</p>
     */
    static SynapseAuditorProvider from(OperationContextProvider provider) {
        OperationContextProvider resolvedProvider = provider == null ? Optional::empty : provider;
        return new SynapseAuditorProvider() {
            @Override
            public Optional<String> currentAuditor() {
                return resolvedProvider.currentActorId();
            }

            @Override
            public Optional<String> currentTenantId() {
                return resolvedProvider.currentTenantId();
            }
        };
    }
}
