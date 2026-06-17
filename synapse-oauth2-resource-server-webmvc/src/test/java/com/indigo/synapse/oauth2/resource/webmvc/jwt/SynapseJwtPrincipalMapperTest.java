package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedPrincipal;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseJwtPrincipalMapperTest {

    private final SynapseJwtPrincipalMapper mapper = new SynapseJwtPrincipalMapper();

    @Test
    void shouldMapUserPrincipal() {
        AuthenticatedPrincipal principal = mapper.map(jwt(Map.of(
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "USER",
                SynapseJwtClaimNames.PREFERRED_USERNAME, "admin",
                SynapseJwtClaimNames.TENANT_ID, "tenant-a",
                SynapseJwtClaimNames.ROLES, List.of("ADMIN"),
                SynapseJwtClaimNames.PERMISSIONS, List.of("message:read")
        )));

        assertThat(principal).isInstanceOf(AuthenticatedUser.class);
        assertThat(principal.principalId()).isEqualTo("subject-1");
        assertThat(principal.displayName()).isEqualTo("admin");
        assertThat(principal.permissions()).containsExactly("message:read");
    }

    @Test
    void shouldMapClientPrincipalWithoutCreatingUser() {
        AuthenticatedPrincipal principal = mapper.map(jwt(Map.of(
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "CLIENT",
                SynapseJwtClaimNames.CLIENT_ID, "client-a",
                SynapseJwtClaimNames.PERMISSIONS, List.of("message:send")
        )));

        assertThat(principal).isInstanceOf(AuthenticatedClient.class);
        assertThat(principal).isNotInstanceOf(AuthenticatedUser.class);
        assertThat(principal.principalId()).isEqualTo("client-a");
        assertThat(principal.permissions()).containsExactly("message:send");
    }

    private static Jwt jwt(Map<String, Object> claims) {
        java.util.Map<String, Object> allClaims = new java.util.LinkedHashMap<>(claims);
        allClaims.put("sub", "subject-1");
        return new Jwt(
                "token",
                Instant.parse("2027-06-17T00:00:00Z"),
                Instant.parse("2027-06-17T01:00:00Z"),
                Map.of("alg", "none"),
                allClaims
        );
    }
}
