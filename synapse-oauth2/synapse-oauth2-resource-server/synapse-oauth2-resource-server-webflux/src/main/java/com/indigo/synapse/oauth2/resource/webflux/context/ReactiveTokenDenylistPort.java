package com.indigo.synapse.oauth2.resource.webflux.context;

import reactor.core.publisher.Mono;

/**
 * Reactive token denylist 端口。
 */
@FunctionalInterface
public interface ReactiveTokenDenylistPort {

    Mono<Boolean> isDenied(String tokenId);
}
