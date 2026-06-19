package com.indigo.synapse.oauth2.resource.core;

import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import com.indigo.synapse.oauth2.core.validation.JwtValidationResult;
import com.indigo.synapse.security.context.AuthenticatedClient;
import com.indigo.synapse.security.context.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceServerCoreContractTest {

    @Test
    void shouldUseOnePolicyForValidationAndPrincipalMapping() {
        ResourceServerValidationPolicy policy = policy(false);
        JwtClaimAccessor claims = claims(Map.of(
                "sub", "user-1",
                "exp", "1",
                "iat", "1",
                "token_type", "ACCESS_TOKEN",
                "principal_type", "USER",
                "aud", List.of("orders"),
                "roles", List.of("admin"),
                "permissions", List.of("order:read")
        ));

        JwtValidationResult result = ResourceServerValidatorFactory.create(policy, null).validate(claims);
        AuthenticatedUser principal = (AuthenticatedUser) new SynapsePrincipalClaimMapper().map(claims);

        assertThat(result.success()).isTrue();
        assertThat(principal.userId()).isEqualTo("user-1");
        assertThat(new SynapseAuthorityClaimMapper().map(claims))
                .containsExactly("ROLE_admin", "PERM_order:read");
    }

    @Test
    void shouldMapClientWithoutPretendingItIsAUser() {
        JwtClaimAccessor claims = claims(Map.of(
                "principal_type", "CLIENT", "sub", "service-sub", "client_id", "billing-client"));

        assertThat(new SynapsePrincipalClaimMapper().map(claims))
                .isInstanceOfSatisfying(AuthenticatedClient.class,
                        client -> assertThat(client.clientId()).isEqualTo("billing-client"));
    }

    @Test
    void shouldRejectDenylistWithoutRealStore() {
        assertThatThrownBy(() -> ResourceServerValidatorFactory.create(policy(true), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TokenDenylistPort");
    }

    private static ResourceServerValidationPolicy policy(boolean denylist) {
        return new ResourceServerValidationPolicy(true, "https://issuer.example", true,
                List.of("orders"), List.of(SynapseTokenType.ACCESS_TOKEN),
                ResourceServerValidationPolicy.DEFAULT_REQUIRED_CLAIMS, Duration.ofSeconds(60), denylist);
    }

    private static JwtClaimAccessor claims(Map<String, Object> values) {
        return new JwtClaimAccessor() {
            @Override
            public Optional<String> string(String name) {
                Object value = values.get(name);
                return value instanceof String text ? Optional.of(text) : Optional.empty();
            }

            @Override
            public Collection<String> strings(String name) {
                Object value = values.get(name);
                if (value instanceof Collection<?> collection) {
                    return collection.stream().map(String::valueOf).toList();
                }
                return value instanceof String text ? List.of(text) : List.of();
            }
        };
    }
}
