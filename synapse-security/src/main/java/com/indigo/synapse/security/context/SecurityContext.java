package com.indigo.synapse.security.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;

import java.util.Optional;

/**
 * 当前线程安全上下文。
 *
 * <p>SecurityContext 保存当前请求或当前执行作用域中的 {@link AuthenticatedPrincipal}。打开作用域时，
 * 会同步把主体适配为 core 的 {@link OperationContext}，使 data、audit、message 等模块可以通过
 * OperationContext 读取操作人信息，而不需要依赖 security。</p>
 *
 * <p>该类型基于 ThreadLocal，使用 Servlet 线程池、异步执行、任务或消息消费时必须确保执行结束后清理，</p>
 */
public final class SecurityContext {

    private static final ThreadLocal<AuthenticatedPrincipal> CURRENT_PRINCIPAL = new ThreadLocal<>();

    private SecurityContext() {
    }

    /**
     * 打开当前已认证主体作用域，并同步建立 OperationContext 作用域。
     *
     * @param principal 已认证主体；传入 null 时清理上下文
     * @return 可关闭作用域
     */
    public static SecurityContextScope openScope(AuthenticatedPrincipal principal) {
        AuthenticatedPrincipal previousPrincipal = CURRENT_PRINCIPAL.get();
        if (principal == null) {
            CURRENT_PRINCIPAL.remove();
        } else {
            CURRENT_PRINCIPAL.set(principal);
        }
        OperationContext operationContext = principal == null ? null : SecurityOperationContextAdapter.toOperationContext(principal);
        return new SecurityContextScope(previousPrincipal, OperationContextHolder.scope(operationContext));
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

    static void setPrincipalOnly(AuthenticatedPrincipal principal) {
        CURRENT_PRINCIPAL.set(principal);
    }

    static void clearPrincipalOnly() {
        CURRENT_PRINCIPAL.remove();
    }

}
