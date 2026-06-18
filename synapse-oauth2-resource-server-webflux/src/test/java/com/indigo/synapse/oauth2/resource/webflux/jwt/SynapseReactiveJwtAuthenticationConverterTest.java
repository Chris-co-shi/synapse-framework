package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedClient;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SynapseReactiveJwtAuthenticationConverterTest {

    @Test
    void shouldMapClientWithoutCreatingUser() {
        SynapseReactiveJwtAuthenticationConverter converter = new SynapseReactiveJwtAuthenticationConverter();
        Jwt jwt = new Jwt(
                "token",
                Instant.parse("2027-06-17T00:00:00Z"),
                Instant.parse("2027-06-17T01:00:00Z"),
                Map.of("alg", "none"),
                Map.of(
                        SynapseJwtClaimNames.SUBJECT, "client-subject",
                        SynapseJwtClaimNames.PRINCIPAL_TYPE, "CLIENT",
                        SynapseJwtClaimNames.CLIENT_ID, "client-a",
                        SynapseJwtClaimNames.PERMISSIONS, List.of("message:send")
                )
        );

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication.getDetails()).isInstanceOf(AuthenticatedClient.class);
                    AuthenticatedClient client = (AuthenticatedClient) authentication.getDetails();
                    assertThat(client.clientId()).isEqualTo("client-a");
                    assertThat(client.permissions()).containsExactly("message:send");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectUnsupportedPrincipalType() {
        SynapseReactiveJwtAuthenticationConverter converter =
                new SynapseReactiveJwtAuthenticationConverter();

        Jwt jwt = new Jwt(
                "token",
                Instant.parse("2027-06-17T00:00:00Z"),
                Instant.parse("2027-06-17T01:00:00Z"),
                Map.of("alg", "none"),
                Map.of(
                        SynapseJwtClaimNames.SUBJECT, "principal-1",
                        SynapseJwtClaimNames.PRINCIPAL_TYPE, "SERVICE"
                )
        );

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported principal_type");
    }
}
