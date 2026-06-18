package com.indigo.synapse.security.context;

import com.indigo.synapse.security.context.internal.SecurityContextState;

import java.util.Optional;

/**
 * 当前线程的只读安全上下文。
 *
 * <p>认证主体只能由 Framework 的可信认证适配器绑定。
 * 业务代码只能通过本类型读取当前主体，不能建立、替换或清理认证身份。</p>
 *
 * <p>该上下文基于 ThreadLocal，不会自动传播到异步线程、
 * 定时任务或消息消费线程。</p>
 */
public final class SecurityContext {

    private SecurityContext() {
    }

    /**
     * 返回当前已认证主体。
     */
    public static Optional<AuthenticatedPrincipal> currentPrincipal() {
        return Optional.ofNullable(
                SecurityContextState.currentPrincipal()
        );
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
}
