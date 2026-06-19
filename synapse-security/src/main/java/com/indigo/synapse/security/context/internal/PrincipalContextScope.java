package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;

/**
 * 当前主体上下文的内部作用域。
 *
 * <p>关闭时恢复进入作用域前的认证主体和 OperationContext。重复关闭是幂等操作，
 * 便于 Servlet 异常链和 try-with-resources 统一执行清理。</p>
 */
public final class PrincipalContextScope implements AutoCloseable {

    private final AuthenticatedPrincipal previousPrincipal;
    private final OperationContextScope operationContextScope;
    private boolean closed;

    PrincipalContextScope(
            AuthenticatedPrincipal previousPrincipal,
            OperationContextScope operationContextScope
    ) {
        this.previousPrincipal = previousPrincipal;
        this.operationContextScope = operationContextScope;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }

        closed = true;

        try {
            PrincipalContextState.setPrincipal(previousPrincipal);
        } finally {
            operationContextScope.close();
        }
    }
}
