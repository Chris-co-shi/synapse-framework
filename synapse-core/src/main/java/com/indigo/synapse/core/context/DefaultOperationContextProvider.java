package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 基于 {@link OperationContextHolder} 的默认操作上下文读取实现。
 */
public final class DefaultOperationContextProvider implements OperationContextProvider {

    @Override
    public Optional<OperationContext> current() {
        return OperationContextHolder.current();
    }
}
