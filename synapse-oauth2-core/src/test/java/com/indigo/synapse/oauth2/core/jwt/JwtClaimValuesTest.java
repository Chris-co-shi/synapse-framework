package com.indigo.synapse.oauth2.core.jwt;

import com.indigo.synapse.oauth2.core.validation.JwtClaimAccessor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtClaimValuesTest {

    @Test
    void shouldReadRequiredStringClaim() {
        JwtClaimAccessor claims = accessor(
                Map.of(SynapseJwtClaimNames.SUBJECT, "user-1")
        );

        String subject = JwtClaimValues.requiredString(
                claims,
                SynapseJwtClaimNames.SUBJECT
        );

        assertThat(subject).isEqualTo("user-1");
    }

    @Test
    void shouldRejectBlankRequiredStringClaim() {
        JwtClaimAccessor claims = accessor(
                Map.of(SynapseJwtClaimNames.SUBJECT, " ")
        );

        assertThatThrownBy(() ->
                JwtClaimValues.requiredString(
                        claims,
                        SynapseJwtClaimNames.SUBJECT
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sub must not be blank");
    }


    @Test
    void shouldNormalizeStringClaimValues() {
        JwtClaimAccessor claims = accessor(
                Map.of(),
                Map.of(
                        SynapseJwtClaimNames.ROLES,
                        List.of(" admin ", "", "operator", "admin")
                )
        );

        assertThat(
                JwtClaimValues.strings(
                        claims,
                        SynapseJwtClaimNames.ROLES
                )
        ).containsExactly("admin", "operator");
    }

    @Test
    void shouldReturnEmptySetWhenStringClaimIsMissing() {
        JwtClaimAccessor claims = accessor(
                Map.of(),
                Map.of()
        );

        assertThat(
                JwtClaimValues.strings(
                        claims,
                        SynapseJwtClaimNames.PERMISSIONS
                )
        ).isEmpty();
    }

    private static JwtClaimAccessor accessor(Map<String, String> values) {
        return accessor(values, Map.of());
    }

    private static JwtClaimAccessor accessor(
            Map<String, String> values,
            Map<String, Collection<String>> collectionValues
    ) {
        return new JwtClaimAccessor() {

            @Override
            public Optional<String> string(String name) {
                return Optional.ofNullable(values.get(name));
            }

            @Override
            public Collection<String> strings(String name) {
                return collectionValues.getOrDefault(name, List.of());
            }
        };
    }
}