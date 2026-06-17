package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContextScope;

/**
 * 安全上下文作用域。
 *
 * <p>作用域关闭时会同时恢复进入作用域前的 SecurityContext 和 OperationContext。</p>
 */
public final class SecurityContextScope implements AutoCloseable {

    private final AuthenticatedPrincipal previousPrincipal;
    private final OperationContextScope operationContextScope;
    private boolean closed;

    SecurityContextScope(AuthenticatedPrincipal previousPrincipal, OperationContextScope operationContextScope) {
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
            if (previousPrincipal == null) {
                SecurityContext.clearPrincipalOnly();
            } else {
                SecurityContext.setPrincipalOnly(previousPrincipal);
            }
        } finally {
            operationContextScope.close();
        }
    }
}
