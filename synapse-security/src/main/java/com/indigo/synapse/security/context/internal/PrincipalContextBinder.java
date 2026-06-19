package com.indigo.synapse.security.context.internal;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.core.context.OperationContextHolder;
import com.indigo.synapse.core.context.OperationContextScope;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;

/**
 * {@code CurrentPrincipalContext} 内部绑定入口。
 *
 * <p>仅供 Framework 的可信认证适配器使用，业务代码不应直接调用。</p>
 */
public final class PrincipalContextBinder {

    private PrincipalContextBinder() {
    }

    /**
     * 绑定当前已认证主体，并同步建立 OperationContext 作用域。
     *
     * <p>传入 null 时建立临时空作用域。关闭返回的 Scope 后，会同时恢复进入前的主体和
     * OperationContext。若 OperationContext 作用域创建失败，本方法也会恢复此前主体，
     * 不留下半绑定状态。</p>
     *
     * @param principal 已认证主体；允许为 null
     * @return 可恢复的安全上下文作用域
     */
    public static PrincipalContextScope bind(
            AuthenticatedPrincipal principal
    ) {
        AuthenticatedPrincipal previousPrincipal =
                PrincipalContextState.currentPrincipal();

        OperationContext operationContext =
                principal == null
                        ? null
                        : SecurityOperationContextAdapter
                          .toOperationContext(principal);

        PrincipalContextState.setPrincipal(principal);

        try {
            OperationContextScope operationScope =
                    OperationContextHolder.scope(operationContext);

            return new PrincipalContextScope(
                    previousPrincipal,
                    operationScope
            );
        } catch (RuntimeException | Error exception) {
            PrincipalContextState.setPrincipal(previousPrincipal);
            throw exception;
        }
    }
}
