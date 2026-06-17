package com.indigo.synapse.oauth2.core.validation;

import com.indigo.synapse.oauth2.core.exception.OAuth2ErrorCode;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import com.indigo.synapse.oauth2.core.token.NoopTokenDenylistPort;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseJwtValidatorTest {

    @Test
    void shouldValidateTokenTypeAndPrincipalType() {
        JwtClaimAccessor claims = claims(Map.of(
                SynapseJwtClaimNames.TOKEN_TYPE, "ACCESS_TOKEN",
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "CLIENT",
                SynapseJwtClaimNames.SUBJECT, "client-a",
                SynapseJwtClaimNames.CLIENT_ID, "client-a"
        ));
        SynapseJwtValidator validator = SynapseJwtValidatorFactory.composite(List.of(
                new RequiredClaimsValidator(List.of(SynapseJwtClaimNames.SUBJECT)),
                new TokenTypeValidator(List.of(SynapseTokenType.ACCESS_TOKEN)),
                new PrincipalTypeClaimValidator(),
                new PrincipalClaimsValidator(),
                new DenylistedTokenValidator(new NoopTokenDenylistPort())
        ));

        assertThat(validator.validate(claims).success()).isTrue();
    }

    @Test
    void shouldRejectInvalidPrincipalType() {
        JwtValidationResult result = new PrincipalTypeClaimValidator()
                .validate(claims(Map.of(SynapseJwtClaimNames.PRINCIPAL_TYPE, "SERVICE")));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo(OAuth2ErrorCode.OAUTH2_INVALID_PRINCIPAL_TYPE);
    }

    @Test
    void shouldRejectMissingClientIdForClientPrincipal() {
        JwtValidationResult result = new PrincipalClaimsValidator().validate(claims(Map.of(
                SynapseJwtClaimNames.PRINCIPAL_TYPE, "CLIENT",
                SynapseJwtClaimNames.SUBJECT, "client-a"
        )));

        assertThat(result.success()).isFalse();
        assertThat(result.errorCode()).isEqualTo(OAuth2ErrorCode.OAUTH2_MISSING_REQUIRED_CLAIM);
    }

    private static JwtClaimAccessor claims(Map<String, String> values) {
        return new JwtClaimAccessor() {
            @Override
            public Optional<String> string(String name) {
                return Optional.ofNullable(values.get(name));
            }

            @Override
            public Collection<String> strings(String name) {
                return string(name).map(List::of).orElseGet(List::of);
            }
        };
    }
}
