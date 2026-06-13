package com.indigo.synapse.core.context;

/**
 * 操作上下文作用域。
 *
 * <p>该类型支持 try-with-resources，在关闭时恢复创建作用域前的上下文。</p>
 */
public final class OperationContextScope implements AutoCloseable {

    private final OperationContext previous;
    private boolean closed;

    OperationContextScope(OperationContext previous) {
        this.previous = previous;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        OperationContextHolder.set(previous);
    }
}
