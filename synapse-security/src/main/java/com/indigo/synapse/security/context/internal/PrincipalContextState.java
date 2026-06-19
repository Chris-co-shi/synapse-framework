package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.security.context.AuthenticatedPrincipal;

/**
 * 当前线程认证主体的最小存储。
 *
 * <p>该类型不解析任何认证协议，只为同步调用链提供 ThreadLocal 生命周期存储。
 * 写入能力保持包内可见，避免业务代码绕过 {@link PrincipalContextBinder} 直接修改状态。</p>
 */
public final class PrincipalContextState {

    private static final ThreadLocal<AuthenticatedPrincipal>
            CURRENT_PRINCIPAL = new ThreadLocal<>();

    private PrincipalContextState() {
    }

    /**
     * 返回当前线程已绑定的主体。
     *
     * @return 当前主体；未绑定时为 {@code null}
     */
    public static AuthenticatedPrincipal currentPrincipal() {
        return CURRENT_PRINCIPAL.get();
    }

    /**
     * 替换当前线程主体；传入 {@code null} 时必须调用 remove，防止线程池复用泄漏。
     */
    static void setPrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            CURRENT_PRINCIPAL.remove();
        } else {
            CURRENT_PRINCIPAL.set(principal);
        }
    }
}
