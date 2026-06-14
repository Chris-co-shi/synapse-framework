package com.indigo.synapse.audit.context;

/**
 * 审计上下文作用域。
 *
 * <p>该作用域在关闭时恢复进入作用域前的 AuditContext，避免线程复用造成上下文污染。
 * 调用方不应直接创建本类型，应通过 {@link AuditContext#scope(AuditContextSnapshot)} 获取。</p>
 */
public final class AuditContextScope implements AutoCloseable {

    private final AuditContextSnapshot previous;
    private boolean closed;

    AuditContextScope(AuditContextSnapshot previous) {
        this.previous = previous;
    }

    /**
     * 恢复旧审计上下文。重复关闭是安全的。
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        AuditContext.set(previous);
    }
}
