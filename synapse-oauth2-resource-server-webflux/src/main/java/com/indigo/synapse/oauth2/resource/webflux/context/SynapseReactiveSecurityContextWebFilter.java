package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.internal.SecurityOperationContextAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 将 Reactive Spring SecurityContext 桥接到 Synapse Reactor Context。
 */
public final class SynapseReactiveSecurityContextWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(org.springframework.security.core.context.SecurityContext::getAuthentication)
                .map(this::principal)
                .flatMap(principal -> {
                    if (principal == null) {
                        return chain.filter(exchange);
                    }
                    OperationContext operationContext = SecurityOperationContextAdapter.toOperationContext(principal);
                    return chain.filter(exchange).contextWrite(context -> context
                            .put(SynapseReactiveSecurityContext.PRINCIPAL_KEY, principal)
                            .put(SynapseReactiveOperationContext.OPERATION_CONTEXT_KEY, operationContext));
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token
                && token.getDetails() instanceof AuthenticatedPrincipal principal) {
            return principal;
        }
        return null;
    }
}
