package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedUser;
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
        SynapseReactiveJwtAuthenticationConverter converter =
                new SynapseReactiveJwtAuthenticationConverter();
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "client-subject",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.CLIENT.name(),
                SynapseJwtClaimNames.CLIENT_ID, "client-a",
                SynapseJwtClaimNames.PERMISSIONS, List.of("message:send")
        ));

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
    void shouldNormalizeUserClaimsAndAuthorities() {
        SynapseReactiveJwtAuthenticationConverter converter =
                new SynapseReactiveJwtAuthenticationConverter();
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "user-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.USER.name(),
                SynapseJwtClaimNames.ROLES, " admin  operator admin ",
                SynapseJwtClaimNames.PERMISSIONS,
                List.of(" message:read ", "", "message:read"),
                SynapseJwtClaimNames.SCOPE, "openid  profile openid"
        ));

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication.getDetails()).isInstanceOf(AuthenticatedUser.class);
                    AuthenticatedUser user = (AuthenticatedUser) authentication.getDetails();
                    assertThat(user.roles()).containsExactly("admin", "operator");
                    assertThat(user.permissions()).containsExactly("message:read");
                    assertThat(authentication.getAuthorities())
                            .extracting(authority -> authority.getAuthority())
                            .containsExactly(
                                    "SCOPE_openid",
                                    "SCOPE_profile",
                                    "ROLE_admin",
                                    "ROLE_operator",
                                    "PERM_message:read"
                            );
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectUnsupportedPrincipalType() {
        SynapseReactiveJwtAuthenticationConverter converter =
                new SynapseReactiveJwtAuthenticationConverter();
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "principal-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "SERVICE"
        ));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unsupported principal_type");
    }

    private static Jwt jwt(Map<String, Object> claims) {
        return new Jwt(
                "token",
                Instant.parse("2027-06-17T00:00:00Z"),
                Instant.parse("2027-06-17T01:00:00Z"),
                Map.of("alg", "none"),
                claims
        );
    }
}
