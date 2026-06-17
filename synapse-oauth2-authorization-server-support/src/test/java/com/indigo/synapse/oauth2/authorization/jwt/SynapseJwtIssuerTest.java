package com.indigo.synapse.oauth2.authorization.jwt;

import com.indigo.synapse.oauth2.authorization.jwk.SynapseRsaKeyFactory;
import com.indigo.synapse.oauth2.core.jwt.SynapseJwtClaimNames;
import com.indigo.synapse.oauth2.core.jwt.SynapseTokenType;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SynapseJwtIssuerTest {

    @Test
    void shouldIssueAccessTokenWithoutVerifyApi() throws Exception {
        RSAKey rsaKey = SynapseRsaKeyFactory.generate("test-key");
        SynapseJwtIssuer issuer = new SynapseJwtIssuer(
                new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey))),
                "test-key"
        );

        String token = issuer.issue(new JwtIssuanceClaims(
                "synapse",
                "user-1",
                Set.of("service-a"),
                "token-1",
                SynapseTokenType.ACCESS_TOKEN,
                "USER",
                Instant.parse("2027-06-17T00:00:00Z"),
                Instant.parse("2027-06-17T01:00:00Z"),
                Map.of(SynapseJwtClaimNames.PREFERRED_USERNAME, "admin")
        ));
        Jwt jwt = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build().decode(token);

        assertThat(jwt.getSubject()).isEqualTo("user-1");
        assertThat(jwt.getClaimAsString(SynapseJwtClaimNames.TOKEN_TYPE)).isEqualTo("ACCESS_TOKEN");
        assertThat(jwt.getClaimAsString(SynapseJwtClaimNames.PRINCIPAL_TYPE)).isEqualTo("USER");
        assertThat(jwt.getClaimAsString(SynapseJwtClaimNames.PREFERRED_USERNAME)).isEqualTo("admin");
    }
}
