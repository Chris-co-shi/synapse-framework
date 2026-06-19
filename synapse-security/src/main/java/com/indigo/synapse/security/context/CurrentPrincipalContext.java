package com.indigo.synapse.security.context;

import com.indigo.synapse.security.context.internal.PrincipalContextState;

import java.util.Optional;

/**
 * 当前线程的只读认证主体上下文。
 *
 * <p>认证主体只能由 Framework 的可信认证适配器绑定。
 * 业务代码只能通过本类型读取当前主体，不能建立、替换或清理认证身份。</p>
 *
 * <p>Servlet 等同步入口可以通过 Framework 内部 Binder 在严格作用域内使用该
 * ThreadLocal 门面。异步线程、定时任务、消息消费线程和 Reactive 链路不会自动继承该值；
 * Reactive 适配器必须使用 Reactor Context。</p>
 */
public final class CurrentPrincipalContext {

    private CurrentPrincipalContext() {
    }

    /**
     * 返回当前已认证主体。
     *
     * @return 当前主体；未绑定时为空
     */
    public static Optional<AuthenticatedPrincipal> currentPrincipal() {
        return Optional.ofNullable(
                PrincipalContextState.currentPrincipal()
        );
    }

    /**
     * 返回当前已认证用户。
     *
     * @return 当前主体为用户时返回用户，否则为空
     */
    public static Optional<AuthenticatedUser> currentUser() {
        return currentPrincipal()
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);
    }

    /**
     * 返回当前已认证客户端。
     *
     * @return 当前主体为客户端时返回客户端，否则为空
     */
    public static Optional<AuthenticatedClient> currentClient() {
        return currentPrincipal()
                .filter(AuthenticatedClient.class::isInstance)
                .map(AuthenticatedClient.class::cast);
    }
}
