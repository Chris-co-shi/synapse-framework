package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import reactor.core.publisher.Mono;

/**
 * Reactor Context 中的当前认证主体读取入口。
 *
 * <p>该门面只读取订阅链携带的 Reactor Context，不回退到 ThreadLocal，因此在
 * {@code publishOn}、{@code subscribeOn} 和并发请求场景中不会读取其他线程或请求的主体。</p>
 */
public final class ReactiveCurrentPrincipalContext {

    /**
     * Reactor Context 中保存认证主体的类型键。
     */
    public static final Class<AuthenticatedPrincipal> PRINCIPAL_KEY = AuthenticatedPrincipal.class;

    private ReactiveCurrentPrincipalContext() {
    }

    /**
     * 返回当前订阅链中的认证主体。
     */
    public static Mono<AuthenticatedPrincipal> currentPrincipal() {
        return Mono.deferContextual(context -> context.hasKey(PRINCIPAL_KEY)
                ? Mono.just(context.get(PRINCIPAL_KEY))
                : Mono.empty());
    }

    /**
     * 当前主体为用户时返回用户。
     */
    public static Mono<AuthenticatedUser> currentUser() {
        return currentPrincipal().filter(AuthenticatedUser.class::isInstance).cast(AuthenticatedUser.class);
    }

    /**
     * 当前主体为客户端时返回客户端。
     */
    public static Mono<AuthenticatedClient> currentClient() {
        return currentPrincipal().filter(AuthenticatedClient.class::isInstance).cast(AuthenticatedClient.class);
    }
}
