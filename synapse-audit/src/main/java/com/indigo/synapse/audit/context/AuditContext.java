package com.indigo.synapse.audit.context;

import com.indigo.synapse.audit.event.AuditSubject;

import java.util.Optional;

/**
 * 当前线程审计上下文。
 *
 * <p>AuditContext 用于少数需要显式指定审计主体和 traceId 的场景，例如补偿任务、批处理、内部调用或
 * 无法直接从 OperationContext 推导主体的执行链路。它不是安全上下文，也不替代 core OperationContext。</p>
 *
 * <p>该类型基于 ThreadLocal，使用后必须清理。推荐通过 {@link #scope(AuditContextSnapshot)} 配合
 * try-with-resources 使用。</p>
 */
public final class AuditContext {

    private static final ThreadLocal<AuditContextSnapshot> CURRENT = new ThreadLocal<>();

    private AuditContext() {
    }

    /**
     * 设置当前审计上下文；传入 null 时清理。
     */
    public static void set(AuditContextSnapshot snapshot) {
        if (snapshot == null) {
            clear();
            return;
        }
        CURRENT.set(snapshot);
    }

    /**
     * 建立审计上下文作用域，并在关闭时恢复旧上下文。
     */
    public static AuditContextScope scope(AuditContextSnapshot snapshot) {
        AuditContextSnapshot previous = CURRENT.get();
        set(snapshot);
        return new AuditContextScope(previous);
    }

    /**
     * 返回当前审计上下文。
     */
    public static Optional<AuditContextSnapshot> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * 返回当前审计主体。
     */
    public static Optional<AuditSubject> currentSubject() {
        return current().map(AuditContextSnapshot::subject);
    }

    /**
     * 返回当前 traceId。
     */
    public static Optional<String> currentTraceId() {
        return current().map(AuditContextSnapshot::traceId);
    }

    /**
     * 清理当前审计上下文。
     */
    public static void clear() {
        CURRENT.remove();
    }
}
