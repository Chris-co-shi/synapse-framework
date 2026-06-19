package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapsePrincipalType;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynapseJwtPrincipalMapperTest {

    private final SynapseJwtPrincipalMapper mapper = new SynapseJwtPrincipalMapper();

    @Test
    void shouldMapUserPrincipal() {
        AuthenticatedPrincipal principal = mapper.map(jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "subject-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.USER.name(),
                SynapseJwtClaimNames.PREFERRED_USERNAME, "admin",
                SynapseJwtClaimNames.TENANT_ID, "tenant-a",
                SynapseJwtClaimNames.ROLES, List.of(" ADMIN ", "ADMIN"),
                SynapseJwtClaimNames.PERMISSIONS, List.of(" message:read ", "message:read")
        )));

        assertThat(principal).isInstanceOf(AuthenticatedUser.class);
        assertThat(principal.principalId()).isEqualTo("subject-1");
        assertThat(principal.displayName()).isEqualTo("admin");
        assertThat(principal.roles()).containsExactly("ADMIN");
        assertThat(principal.permissions()).containsExactly("message:read");
    }

    @Test
    void shouldMapClientPrincipalWithoutCreatingUser() {
        AuthenticatedPrincipal principal = mapper.map(jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "client-subject",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.CLIENT.name(),
                SynapseJwtClaimNames.CLIENT_ID, "client-a",
                SynapseJwtClaimNames.PERMISSIONS, List.of("message:send")
        )));

        assertThat(principal).isInstanceOf(AuthenticatedClient.class);
        assertThat(principal).isNotInstanceOf(AuthenticatedUser.class);
        assertThat(principal.principalId()).isEqualTo("client-a");
        assertThat(principal.permissions()).containsExactly("message:send");
    }

    @Test
    void shouldRejectUnsupportedPrincipalType() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "principal-1",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "SERVICE"
        ));

        assertThatThrownBy(() -> mapper.map(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("unsupported principal_type: SERVICE");
    }

    @Test
    void shouldRejectMissingPrincipalType() {
        Jwt jwt = jwt(Map.of(SynapseJwtClaimNames.SUBJECT, "user-1"));

        assertThatThrownBy(() -> mapper.map(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("principal_type must not be blank");
    }

    @Test
    void shouldRejectMissingSubjectForUser() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.USER.name()
        ));

        assertThatThrownBy(() -> mapper.map(jwt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sub must not be blank");
    }

    @Test
    void shouldRejectMissingClientIdForClient() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SUBJECT, "client-subject",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, SynapsePrincipalType.CLIENT.name()
        ));

        assertThatThrownBy(() -> mapper.map(jwt))
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
