package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.internal.SecurityOperationContextAdapter;
import reactor.core.publisher.Mono;

/**
 * Reactor Context 中的 OperationContext 读取入口。
 */
public final class SynapseReactiveOperationContext {

    public static final Class<OperationContext> OPERATION_CONTEXT_KEY = OperationContext.class;

    private SynapseReactiveOperationContext() {
    }

    public static Mono<OperationContext> currentOperationContext() {
        return Mono.deferContextual(context -> {
            if (context.hasKey(OPERATION_CONTEXT_KEY)) {
                return Mono.just(context.get(OPERATION_CONTEXT_KEY));
            }
            if (context.hasKey(SynapseReactiveSecurityContext.PRINCIPAL_KEY)) {
                AuthenticatedPrincipal principal = context.get(SynapseReactiveSecurityContext.PRINCIPAL_KEY);
                return Mono.just(SecurityOperationContextAdapter.toOperationContext(principal));
            }
            return Mono.empty();
        });
    }
}
