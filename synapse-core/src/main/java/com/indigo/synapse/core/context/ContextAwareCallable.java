package com.indigo.synapse.core.context;

import java.util.concurrent.Callable;

/**
 * 携带 OperationContextSnapshot 的 Callable。
 *
 * @param <V> 返回值类型
 */
public final class ContextAwareCallable<V> implements Callable<V> {

    private final OperationContextSnapshot snapshot;
    private final Callable<V> delegate;

    public ContextAwareCallable(OperationContextSnapshot snapshot, Callable<V> delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.snapshot = snapshot;
        this.delegate = delegate;
    }

    @Override
    public V call() throws Exception {
        try (OperationContextScope ignored = OperationContextHolder.restore(snapshot)) {
            return delegate.call();
        }
    }
}
