package com.indigo.synapse.oauth2.resource.webmvc.jwt;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseJwtGrantedAuthoritiesConverterTest {

    private final SynapseJwtGrantedAuthoritiesConverter converter =
            new SynapseJwtGrantedAuthoritiesConverter();

    @Test
    void shouldNormalizeAuthoritiesAndKeepProtocolOrder() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SCOPE, " openid  profile openid SCOPE_email ",
                SynapseJwtClaimNames.ROLES,
                List.of(" admin ", "", "admin", "ROLE_operator"),
                SynapseJwtClaimNames.PERMISSIONS,
                List.of(" message:read ", "PERM_message:write", "message:read", " ")
        ));

        assertThat(converter.convert(jwt))
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
    }

    @Test
    void shouldIgnoreMissingBlankAndNonStringClaims() {
        Jwt jwt = jwt(Map.of(
                SynapseJwtClaimNames.SCOPE, 42,
                SynapseJwtClaimNames.ROLES, List.of(1, 2),
                SynapseJwtClaimNames.PERMISSIONS, List.of("", " ")
        ));

        assertThat(converter.convert(jwt)).isEmpty();
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
