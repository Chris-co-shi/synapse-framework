package com.indigo.synapse.core.context;

import java.util.Optional;

/**
 * 当前线程操作上下文持有器。
 *
 * <p>该类型基于 {@link ThreadLocal} 保存当前操作上下文，适用于一次同步调用链内的 data、audit、mq、
 * security 等模块读取共同的操作元数据。它不是全局状态存储，也不会自动跨线程传播。</p>
 *
 * <p>使用线程池、异步执行、消息消费或定时任务时，调用方必须显式创建、快照或恢复上下文。
 * 推荐使用 {@link #scope(OperationContext)} 或 {@link #restore(OperationContextSnapshot)} 并配合
 * try-with-resources，确保执行结束后恢复旧上下文，避免线程复用导致上下文污染。</p>
 */
public final class OperationContextHolder {

    private static final ThreadLocal<OperationContext> CURRENT = new ThreadLocal<>();

    private OperationContextHolder() {
    }

    /**
     * 返回当前线程中的操作上下文。
     *
     * @return 当前上下文；没有上下文时返回 empty
     */
    public static Optional<OperationContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * 返回当前线程中的操作上下文；如果不存在则抛出异常。
     *
     * <p>该方法适合明确要求必须存在上下文的基础设施逻辑。普通业务调用更建议使用
     * {@link OperationContextProvider#current()} 或 {@link #current()} 做显式空值处理。</p>
     *
     * @return 当前操作上下文
     */
    public static OperationContext requireCurrent() {
        return current().orElseThrow(() -> new IllegalStateException("operation context is not available"));
    }

    /**
     * 设置当前线程上下文；传入 null 等价于清理上下文。
     *
     * @param context 操作上下文
     */
    public static void set(OperationContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT.set(context);
    }

    /**
     * 清理当前线程上下文。
     */
    public static void clear() {
        CURRENT.remove();
    }

    /**
     * 创建当前线程上下文快照。
     *
     * <p>快照只保存上下文对象本身，不负责序列化为 HTTP Header 或消息 Header。</p>
     *
     * @return 操作上下文快照
     */
    public static OperationContextSnapshot snapshot() {
        return new OperationContextSnapshot(CURRENT.get());
    }

    /**
     * 建立新的上下文作用域，并在作用域关闭时恢复旧上下文。
     *
     * <pre>{@code
     * try (OperationContextScope ignored = OperationContextHolder.scope(context)) {
     *     // execute use case
     * }
     * }</pre>
     *
     * @param context 新上下文
     * @return 可关闭作用域
     */
    public static OperationContextScope scope(OperationContext context) {
        OperationContext previous = CURRENT.get();
        set(context);
        return new OperationContextScope(previous);
    }

    /**
     * 从快照恢复上下文，并在作用域关闭时恢复旧上下文。
     *
     * @param snapshot 操作上下文快照；传入 null 表示清理当前上下文
     * @return 可关闭作用域
     */
    public static OperationContextScope restore(OperationContextSnapshot snapshot) {
        OperationContext previous = CURRENT.get();
        set(snapshot == null ? null : snapshot.context());
        return new OperationContextScope(previous);
    }
}
