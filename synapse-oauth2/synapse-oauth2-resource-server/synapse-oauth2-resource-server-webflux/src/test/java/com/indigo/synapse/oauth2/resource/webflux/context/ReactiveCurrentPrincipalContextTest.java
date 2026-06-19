package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReactiveCurrentPrincipalContextTest {

    @Test
    void shouldReadPrincipalAndOperationContextFromReactorContext() {
        AuthenticatedClient client = new AuthenticatedClient(
                "client-a",
                "client-a",
                "tenant-a",
                Set.of(),
                Set.of("message:send")
        );

        StepVerifier.create(SynapseReactiveOperationContext.currentOperationContext()
                        .contextWrite(Context.of(ReactiveCurrentPrincipalContext.PRINCIPAL_KEY, client)))
                .assertNext(context -> {
                    assertThat(context.actor().type()).isEqualTo(OperationActorType.SERVICE);
                    assertThat(context.actor().id()).isEqualTo("client-a");
                })
                .verifyComplete();
    }

    @Test
    void shouldKeepPrincipalAcrossPublishOnAndSubscribeOn() {
        AuthenticatedUser user = new AuthenticatedUser(
                "user-a",
                "user-a",
                "tenant-a",
                Set.of("USER"),
                Set.of("message:read")
        );

        Mono<String> principalId = ReactiveCurrentPrincipalContext.currentPrincipal()
                .publishOn(Schedulers.parallel())
                .subscribeOn(Schedulers.boundedElastic())
                .map(AuthenticatedPrincipal::principalId)
                .contextWrite(Context.of(
                        ReactiveCurrentPrincipalContext.PRINCIPAL_KEY,
                        user
                ));

        StepVerifier.create(principalId)
                .expectNext("user-a")
                .verifyComplete();
    }

    @Test
    void shouldIsolateConcurrentReactiveRequests() {
        AuthenticatedUser first = new AuthenticatedUser(
                "user-a", "first", "tenant-a", Set.of(), Set.of()
        );
        AuthenticatedUser second = new AuthenticatedUser(
                "user-b", "second", "tenant-b", Set.of(), Set.of()
        );

        Mono<String> firstRequest = currentPrincipalId(first)
                .subscribeOn(Schedulers.parallel());
        Mono<String> secondRequest = currentPrincipalId(second)
                .subscribeOn(Schedulers.parallel());

        StepVerifier.create(Mono.zip(firstRequest, secondRequest))
                .assertNext(result -> {
                    assertThat(result.getT1()).isEqualTo("user-a");
                    assertThat(result.getT2()).isEqualTo("user-b");
                })
                .verifyComplete();
    }

    private static Mono<String> currentPrincipalId(AuthenticatedPrincipal principal) {
        return ReactiveCurrentPrincipalContext.currentPrincipal()
                .publishOn(Schedulers.parallel())
                .map(AuthenticatedPrincipal::principalId)
                .contextWrite(Context.of(
                        ReactiveCurrentPrincipalContext.PRINCIPAL_KEY,
                        principal
                ));
    }
}
