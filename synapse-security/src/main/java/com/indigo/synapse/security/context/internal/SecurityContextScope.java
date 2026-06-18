package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;

/**
 * SecurityContext 内部作用域。
 *
 * <p>关闭时恢复进入作用域前的安全主体和 OperationContext。</p>
 */
public final class SecurityContextScope implements AutoCloseable {

    private final AuthenticatedPrincipal previousPrincipal;
    private final OperationContextScope operationContextScope;
    private boolean closed;

    SecurityContextScope(
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
            SecurityContextState.setPrincipal(previousPrincipal);
        } finally {
            operationContextScope.close();
        }
    }
}