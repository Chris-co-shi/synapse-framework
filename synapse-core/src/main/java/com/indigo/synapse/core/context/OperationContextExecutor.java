package com.indigo.synapse.core.context;

import java.util.concurrent.Callable;

/**
 * OperationContext 快照包装工具。
 */
public final class OperationContextExecutor {

    private OperationContextExecutor() {
    }

    public static Runnable wrap(Runnable delegate) {
        return new ContextAwareRunnable(OperationContextHolder.snapshot(), delegate);
    }

    public static <V> Callable<V> wrap(Callable<V> delegate) {
        return new ContextAwareCallable<>(OperationContextHolder.snapshot(), delegate);
    }
}
