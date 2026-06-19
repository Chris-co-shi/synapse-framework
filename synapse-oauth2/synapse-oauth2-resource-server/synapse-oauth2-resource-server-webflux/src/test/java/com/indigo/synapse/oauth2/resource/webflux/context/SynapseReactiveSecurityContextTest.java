package com.indigo.synapse.oauth2.resource.webflux.context;

import com.indigo.synapse.core.context.OperationActorType;
import com.indigo.synapse.security.context.AuthenticatedClient;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseReactiveSecurityContextTest {

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
                        .contextWrite(Context.of(SynapseReactiveSecurityContext.PRINCIPAL_KEY, client)))
                .assertNext(context -> {
                    assertThat(context.actor().type()).isEqualTo(OperationActorType.SERVICE);
                    assertThat(context.actor().id()).isEqualTo("client-a");
                })
                .verifyComplete();
    }
}
