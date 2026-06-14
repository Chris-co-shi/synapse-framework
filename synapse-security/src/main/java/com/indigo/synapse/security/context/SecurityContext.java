package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import java.util.Optional;

/**
 * 当前线程安全上下文。
 *
 * <p>SecurityContext 保存当前请求或当前执行作用域中的 {@link AuthenticatedUser}。设置用户时，
 * 会同步把用户适配为 core 的 {@link OperationContext}，使 data、audit、message 等模块可以通过
 * OperationContext 读取操作人信息，而不需要依赖 security。</p>
 *
 * <p>该类型基于 ThreadLocal，使用 Servlet 线程池、异步执行、任务或消息消费时必须确保执行结束后清理，
 * 否则可能污染后续复用线程。trusted-header Filter 会在 finally 中调用 {@link #clear()}。</p>
 */
public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<OperationContextScope> OPERATION_CONTEXT_SCOPE = new ThreadLocal<>();

    private SecurityContext() {
    }

    /**
     * 设置当前已认证用户，并同步建立 OperationContext 作用域。
     *
     * @param authenticatedUser 已认证用户；传入 null 时清理上下文
     */
    public static void set(AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            clear();
            return;
        }
        closeOperationContextScope();
        CURRENT_USER.set(authenticatedUser);
        OperationContext operationContext = SecurityOperationContextAdapter.toOperationContext(authenticatedUser);
        OPERATION_CONTEXT_SCOPE.set(OperationContextHolder.scope(operationContext));
    }

    /**
     * 返回当前已认证用户。
     */
    public static Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(CURRENT_USER.get());
    }

    /**
     * 清理当前安全上下文，并恢复进入安全上下文前的 OperationContext。
     */
    public static void clear() {
        CURRENT_USER.remove();
        closeOperationContextScope();
    }

    /**
     * 当当前没有认证用户时执行清理。
     *
     * <p>该方法用于某些防御性清理场景；正常 Filter 生命周期应直接调用 {@link #clear()}。</p>
     */
    public static void clearIfEmpty() {
        if (CURRENT_USER.get() == null) {
            CURRENT_USER.remove();
            closeOperationContextScope();
        }
    }

    private static void closeOperationContextScope() {
        OperationContextScope scope = OPERATION_CONTEXT_SCOPE.get();
        if (scope == null) {
            return;
        }
        try {
            scope.close();
        } finally {
            OPERATION_CONTEXT_SCOPE.remove();
        }
    }
}
