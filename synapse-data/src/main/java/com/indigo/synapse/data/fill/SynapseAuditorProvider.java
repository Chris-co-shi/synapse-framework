package com.indigo.synapse.data.fill;

import com.indigo.synapse.core.context.OperationContextProvider;

import java.util.Optional;

/**
 * 数据字段自动填充使用的当前审计信息读取接口。
 *
 * <p>该接口只为 data 模块提供当前操作人和租户读取能力。默认实现从 core 的 OperationContextProvider
 * 读取，因此 data 模块不需要依赖 security、web 或业务用户表。</p>
 */
@FunctionalInterface
public interface SynapseAuditorProvider {

    /**
     * 返回当前操作人标识。
     */
    Optional<String> currentAuditor();

    /**
     * 返回当前租户标识。
     *
     * <p>一阶段不实现多租户，只保留 tenantId 字段填充入口。</p>
     */
    default Optional<String> currentTenantId() {
        return Optional.empty();
    }

    /**
     * 返回空审计信息读取器。
     */
    static SynapseAuditorProvider empty() {
        return Optional::empty;
    }

    /**
     * 基于通用操作上下文创建数据填充使用的审计信息读取器。
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
