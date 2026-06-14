package com.indigo.synapse.core.context;

/**
 * 操作上下文作用域。
 *
 * <p>该类型用于 try-with-resources 场景，在关闭时恢复创建作用域前的上下文。它解决的是线程复用下的
 * 上下文清理问题，尤其适用于 Filter、Interceptor、MQ 消费、任务调度、异步包装器等基础设施入口。</p>
 *
 * <p>调用方不应手动 new 本类型，应通过 {@link OperationContextHolder#scope(OperationContext)} 或
 * {@link OperationContextHolder#restore(OperationContextSnapshot)} 创建。</p>
 */
public final class OperationContextScope implements AutoCloseable {

    private final OperationContext previous;
    private boolean closed;

    OperationContextScope(OperationContext previous) {
        this.previous = previous;
    }

    /**
     * 恢复进入作用域前的上下文。
     *
     * <p>重复关闭是安全的，只有第一次关闭会执行恢复。</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        OperationContextHolder.set(previous);
    }
}
