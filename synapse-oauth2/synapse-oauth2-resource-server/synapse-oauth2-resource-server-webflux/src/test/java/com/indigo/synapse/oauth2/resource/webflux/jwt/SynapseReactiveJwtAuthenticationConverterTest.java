package com.indigo.synapse.oauth2.resource.webflux.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseReactiveJwtAuthenticationConverterTest {

    private final SynapseReactiveJwtAuthenticationConverter converter =
            new SynapseReactiveJwtAuthenticationConverter();

    @Test
    void shouldMapClientWithoutCreatingUser() {
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
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "user-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.USER.name(),
                SynapseJwtClaimNames.ROLES,
                List.of(" admin ", "", "admin", "ROLE_operator"),
                SynapseJwtClaimNames.PERMISSIONS,
                List.of(" message:read ", "PERM_message:write", "message:read", " "),
                SynapseJwtClaimNames.SCOPE, " openid  profile openid SCOPE_email "
        ));

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication.getDetails())
                            .isInstanceOf(AuthenticatedUser.class);

                    AuthenticatedUser user =
                            (AuthenticatedUser) authentication.getDetails();

                    assertThat(user.roles())
                            .containsExactlyInAnyOrder("admin", "ROLE_operator");

                    assertThat(user.permissions())
                            .containsExactlyInAnyOrder("message:read", "PERM_message:write");

                    assertThat(authentication.getAuthorities())
                            .extracting(GrantedAuthority::getAuthority)
                            .containsExactly(
                                    "SCOPE_openid",
                                    "SCOPE_profile",
                                    "SCOPE_email",
                                    "ROLE_admin",
                                    "ROLE_operator",
                                    "PERM_message:read",
                                    "PERM_message:write"
                            );
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectUnsupportedPrincipalType() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "principal-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "SERVICE"
        ));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported principal_type: SERVICE");
    }

    @Test
    void shouldRejectMissingPrincipalType() {
        Jwt jwt = jwt(Map.of(SynapseJwtClaimNames.SUBJECT, "user-1"));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("principal_type must not be blank");
    }

    @Test
    void shouldRejectMissingSubjectForUser() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.USER.name()
        ));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sub must not be blank");
    }

    @Test
    void shouldRejectMissingClientIdForClient() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "client-subject",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.CLIENT.name()
        ));

        assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("client_id must not be blank");
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
