package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 当前线程操作上下文持有器。
 *
 * <p>使用线程池或异步执行时必须通过 {@link OperationContextScope} 关闭后恢复旧上下文，
 * 避免上下文污染后续任务。</p>
 */
public final class OperationContextHolder {

    private static final ThreadLocal<OperationContext> CURRENT = new ThreadLocal<>();

    private OperationContextHolder() {
    }

    public static Optional<OperationContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static OperationContext requireCurrent() {
        return current().orElseThrow(() -> new IllegalStateException("operation context is not available"));
    }

    public static void set(OperationContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static OperationContextSnapshot snapshot() {
        return new OperationContextSnapshot(CURRENT.get());
    }

    public static OperationContextScope scope(OperationContext context) {
        OperationContext previous = CURRENT.get();
        set(context);
        return new OperationContextScope(previous);
    }

    public static OperationContextScope restore(OperationContextSnapshot snapshot) {
        OperationContext previous = CURRENT.get();
        set(snapshot == null ? null : snapshot.context());
        return new OperationContextScope(previous);
    }
}
