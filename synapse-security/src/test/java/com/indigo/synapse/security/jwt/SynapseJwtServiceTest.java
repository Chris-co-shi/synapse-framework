package com.indigo.synapse.security.jwt;

import com.indigo.synapse.security.jwk.SynapseRsaKeyFactory;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SynapseJwtServiceTest {

    @Test
    void shouldIssueAndVerifyJwtWithJwk() throws Exception {
        RSAKey rsaKey = SynapseRsaKeyFactory.generate("kid-test");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
        JwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        SynapseJwtService jwtService = new SynapseJwtService(encoder, decoder, "kid-test");
        Instant now = Instant.now();
        JwtClaims claims = new JwtClaims(
                "synapse",
                "user-1",
                Set.of("admin-api"),
                "jti-1",
                JwtTokenType.ACCESS_TOKEN,
                now,
                now.plusSeconds(300)
        );

        String token = jwtService.issue(claims);
        JwtClaims verified = jwtService.verify(token);

        assertEquals(claims.issuer(), verified.issuer());
        assertEquals(claims.subject(), verified.subject());
        assertEquals(claims.audience(), verified.audience());
        assertEquals(claims.tokenId(), verified.tokenId());
        assertEquals(claims.tokenType(), verified.tokenType());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        RSAKey rsaKey = SynapseRsaKeyFactory.generate("kid-test");
        SynapseJwtService jwtService = new SynapseJwtService(
                new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey))),
                NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build(),
                "kid-test"
        );

        assertThrows(IllegalArgumentException.class, () -> jwtService.verify(""));
        assertThrows(JwtException.class, () -> jwtService.verify("invalid-token"));
    }
}
