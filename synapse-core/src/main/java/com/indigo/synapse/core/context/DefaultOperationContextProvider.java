package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 基于 {@link OperationContextHolder} 的默认操作上下文读取实现。
 *
 * <p>该实现适用于大多数同步调用链，直接读取当前线程中的 OperationContext。业务系统如需从自定义上下文、
 * Reactor Context、消息消费上下文或其他容器中读取操作信息，可以提供自己的 {@link OperationContextProvider}
 * Bean 替换默认实现。</p>
 */
public final class DefaultOperationContextProvider implements OperationContextProvider {

    @Override
    public Optional<OperationContext> current() {
        return OperationContextHolder.current();
    }
}
