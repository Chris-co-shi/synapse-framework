package com.indigo.synapse.core.context;

/**
 * 携带 OperationContextSnapshot 的 Runnable。
 */
public final class ContextAwareRunnable implements Runnable {

    private final OperationContextSnapshot snapshot;
    private final Runnable delegate;

    public ContextAwareRunnable(OperationContextSnapshot snapshot, Runnable delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }
        this.snapshot = snapshot;
        this.delegate = delegate;
    }

    @Override
    public void run() {
        try (OperationContextScope ignored = OperationContextHolder.restore(snapshot)) {
            delegate.run();
        }
    }
}
