package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationContext;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.internal.SecurityOperationContextAdapter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * 将 Reactive Spring SecurityContext 桥接到 Synapse Reactor Context。
 */
public final class SynapseReactiveSecurityContextWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> Optional.ofNullable(
                        principal(securityContext.getAuthentication())
                ))
                .defaultIfEmpty(Optional.empty())
                .flatMap(optionalPrincipal -> optionalPrincipal
                        .map(principal -> filterWithContext(exchange, chain, principal))
                        .orElseGet(() -> chain.filter(exchange))
                );
    }

    private Mono<Void> filterWithContext(
            ServerWebExchange exchange,
            WebFilterChain chain,
            AuthenticatedPrincipal principal
    ) {
        OperationContext operationContext =
                SecurityOperationContextAdapter.toOperationContext(principal);

        return chain.filter(exchange)
                .contextWrite(context -> context
                        .put(SynapseReactiveSecurityContext.PRINCIPAL_KEY, principal)
                        .put(
                                SynapseReactiveOperationContext.OPERATION_CONTEXT_KEY,
                                operationContext
                        ));
    }

    private AuthenticatedPrincipal principal(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken token
                && token.getDetails() instanceof AuthenticatedPrincipal principal) {
            return principal;
        }
        return null;
    }
}
