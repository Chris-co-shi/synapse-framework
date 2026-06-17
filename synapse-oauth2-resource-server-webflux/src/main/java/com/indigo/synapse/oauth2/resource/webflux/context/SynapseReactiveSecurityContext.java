package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import reactor.core.publisher.Mono;

/**
 * Reactor Context 中的 Synapse 安全上下文读取入口。
 */
public final class SynapseReactiveSecurityContext {

    public static final Class<AuthenticatedPrincipal> PRINCIPAL_KEY = AuthenticatedPrincipal.class;

    private SynapseReactiveSecurityContext() {
    }

    public static Mono<AuthenticatedPrincipal> currentPrincipal() {
        return Mono.deferContextual(context -> context.hasKey(PRINCIPAL_KEY)
                ? Mono.just(context.get(PRINCIPAL_KEY))
                : Mono.empty());
    }

    public static Mono<AuthenticatedUser> currentUser() {
        return currentPrincipal().filter(AuthenticatedUser.class::isInstance).cast(AuthenticatedUser.class);
    }

    public static Mono<AuthenticatedClient> currentClient() {
        return currentPrincipal().filter(AuthenticatedClient.class::isInstance).cast(AuthenticatedClient.class);
    }
}
