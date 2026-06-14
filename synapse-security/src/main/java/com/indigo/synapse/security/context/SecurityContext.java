package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import java.util.Optional;

public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<OperationContextScope> OPERATION_CONTEXT_SCOPE = new ThreadLocal<>();

    private SecurityContext() {
    }

    public static void set(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            clear();
            return;
        }
        closeOperationContextScope();
        CURRENT_USER.set(authenticatedUser);
        OperationContext operationContext = SecurityOperationContextAdapter.toOperationContext(authenticatedUser);
        OPERATION_CONTEXT_SCOPE.set(OperationContextHolder.scope(operationContext));
    }

    public static Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
        closeOperationContextScope();
    }

    public static void clearIfEmpty() {
        if (CURRENT_USER.get() == null) {
            CURRENT_USER.remove();
            closeOperationContextScope();
        }
    }

    private static void closeOperationContextScope() {
        OperationContextScope scope = OPERATION_CONTEXT_SCOPE.get();
        if (scope == null) {
            return;
        }
        try {
            scope.close();
        } finally {
            OPERATION_CONTEXT_SCOPE.remove();
        }
    }
}
