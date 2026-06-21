package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.core.context.OperationContextPropagationKeys;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import com.indigo.synapse.webflux.filter.SynapseWebFluxContextFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.time.Instant;
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

    @Test
    void shouldIgnoreUntrustedIdentityHeadersAndKeepAuthenticatedOperationContext() {
        AuthenticatedUser user = new AuthenticatedUser(
                "verified-user", "Verified User", "tenant-a", Set.of("USER"), Set.of()
        );
        Authentication authentication = authentication(user);
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/items")
                .header(OperationContextPropagationKeys.ACTOR_ID, "forged-user")
                .header(OperationContextPropagationKeys.TENANT_ID, "forged-tenant")
                .header(OperationContextPropagationKeys.INITIATOR_ID, "forged-initiator")
                .build());
        SynapseWebFluxContextFilter technicalFilter = new SynapseWebFluxContextFilter();
        ReactivePrincipalContextWebFilter principalFilter = new ReactivePrincipalContextWebFilter();

        Mono<Void> result = technicalFilter.filter(exchange, currentExchange ->
                principalFilter.filter(currentExchange, ignored ->
                        SynapseReactiveOperationContext.currentOperationContext().doOnNext(context -> {
                            assertThat(context.actor().id()).isEqualTo("verified-user");
                            assertThat(context.tenantId()).isEqualTo("tenant-a");
                            assertThat(context.initiator()).isEqualTo(context.actor());
                        }).then()))
                .contextWrite(org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication));

        StepVerifier.create(result).verifyComplete();
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

    private static Authentication authentication(AuthenticatedPrincipal principal) {
        Instant issuedAt = Instant.parse("2026-06-21T00:00:00Z");
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(principal.principalId())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(300))
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
        authentication.setDetails(principal);
        return authentication;
    }
}
