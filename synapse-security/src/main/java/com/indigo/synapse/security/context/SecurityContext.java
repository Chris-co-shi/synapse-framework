package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;

import java.util.Optional;

/**
 * 当前线程安全上下文。
 *
 * <p>SecurityContext 保存当前请求或当前执行作用域中的 {@link AuthenticatedPrincipal}。打开作用域时，
 * 会同步把主体适配为 core 的 {@link OperationContext}，使 data、audit、message 等模块可以通过
 * OperationContext 读取操作人信息，而不需要依赖 security。</p>
 *
 * <p>该类型基于 ThreadLocal，使用 Servlet 线程池、异步执行、任务或消息消费时必须确保执行结束后清理，
 * 否则可能污染后续复用线程。trusted-header Filter 会在 finally 中调用 {@link #clear()}。</p>
 */
public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedPrincipal> CURRENT_PRINCIPAL = new ThreadLocal<>();
    private static final ThreadLocal<SecurityContextScope> CURRENT_SCOPE = new ThreadLocal<>();

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
        closeCurrentScope();
        CURRENT_SCOPE.set(openScope(authenticatedUser));
    }

    /**
     * 打开当前已认证主体作用域，并同步建立 OperationContext 作用域。
     *
     * @param principal 已认证主体；传入 null 时清理上下文
     * @return 可关闭作用域
     */
    public static SecurityContextScope openScope(AuthenticatedPrincipal principal) {
        if (principal == null) {
            clear();
            return new SecurityContextScope(null, OperationContextHolder.scope(null));
        }
        AuthenticatedPrincipal previous = CURRENT_PRINCIPAL.get();
        CURRENT_PRINCIPAL.set(principal);
        OperationContext operationContext = SecurityOperationContextAdapter.toOperationContext(principal);
        return new SecurityContextScope(previous, OperationContextHolder.scope(operationContext));
    }

    /**
     * 返回当前已认证主体。
     */
    public static Optional<AuthenticatedPrincipal> currentPrincipal() {
        return Optional.ofNullable(CURRENT_PRINCIPAL.get());
    }

    /**
     * 返回当前已认证用户。
     */
    public static Optional<AuthenticatedUser> currentUser() {
        return currentPrincipal()
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);
    }

    /**
     * 返回当前已认证客户端。
     */
    public static Optional<AuthenticatedClient> currentClient() {
        return currentPrincipal()
                .filter(AuthenticatedClient.class::isInstance)
                .map(AuthenticatedClient.class::cast);
    }

    /**
     * 清理当前安全上下文，并恢复进入安全上下文前的 OperationContext。
     */
    public static void clear() {
        SecurityContextScope scope = CURRENT_SCOPE.get();
        if (scope != null) {
            try {
                scope.close();
            } finally {
                CURRENT_SCOPE.remove();
            }
            return;
        }
        CURRENT_PRINCIPAL.remove();
        OperationContextHolder.clear();
    }

    /**
     * 当当前没有认证用户时执行清理。
     *
     * <p>该方法用于某些防御性清理场景；正常 Filter 生命周期应直接调用 {@link #clear()}。</p>
     */
    public static void clearIfEmpty() {
        if (CURRENT_PRINCIPAL.get() == null) {
            CURRENT_PRINCIPAL.remove();
        }
    }

    static void setPrincipalOnly(AuthenticatedPrincipal principal) {
        CURRENT_PRINCIPAL.set(principal);
    }

    static void clearPrincipalOnly() {
        CURRENT_PRINCIPAL.remove();
    }

    private static void closeCurrentScope() {
        SecurityContextScope scope = CURRENT_SCOPE.get();
        if (scope == null) {
            return;
        }
        try {
            scope.close();
        } finally {
            CURRENT_SCOPE.remove();
        }
    }
}
