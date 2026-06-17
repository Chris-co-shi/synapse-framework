package com.indigo.synapse.security.context;

/**
 * 当前线程安全上下文作用域。
 *
 * <p>该作用域由 {@link SecurityContext#scope(AuthenticatedUser)} 创建，并在关闭时恢复进入作用域前的
 * SecurityContext 与 OperationContext。它适合 Filter、任务、异步包装器和消息消费等需要嵌套上下文的入口。</p>
 *
 * <p>重复关闭是安全的。由于底层上下文基于 ThreadLocal，作用域必须在创建它的同一线程关闭。</p>
 */
public final class SecurityContextScope implements AutoCloseable {

    private final Thread ownerThread;
    private final Runnable restoreAction;
    private boolean closed;

    SecurityContextScope(Runnable restoreAction) {
        if (restoreAction == null) {
            throw new IllegalArgumentException("restoreAction must not be null");
        }
        this.ownerThread = Thread.currentThread();
        this.restoreAction = restoreAction;
    }

    /**
     * 恢复进入作用域前的安全上下文与操作上下文。
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        if (Thread.currentThread() != ownerThread) {
            throw new IllegalStateException("security context scope must be closed on the creating thread");
        }
        closed = true;
        restoreAction.run();
    }
}
