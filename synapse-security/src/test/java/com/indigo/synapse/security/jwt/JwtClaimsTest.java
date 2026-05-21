package com.indigo.synapse.security.jwt;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtClaimsTest {

    @Test
    void shouldCreateImmutableClaimsAndCheckExpiration() {
        Instant issuedAt = Instant.parse("2026-05-20T10:00:00Z");
        JwtClaims claims = new JwtClaims(
                "synapse",
                "user-1",
                Set.of("admin-api"),
                "jti-1",
                JwtTokenType.ACCESS_TOKEN,
                issuedAt,
                issuedAt.plusSeconds(300)
        );

        assertFalse(claims.isExpired(issuedAt.plusSeconds(299)));
        assertTrue(claims.isExpired(issuedAt.plusSeconds(300)));
        assertThrows(UnsupportedOperationException.class, () -> claims.audience().add("other"));
    }

    @Test
    void shouldRejectInvalidClaims() {
        Instant now = Instant.parse("2026-05-20T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new JwtClaims("", "u", Set.of(), "jti", JwtTokenType.ACCESS_TOKEN, now, now.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new JwtClaims("i", "", Set.of(), "jti", JwtTokenType.ACCESS_TOKEN, now, now.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new JwtClaims("i", "u", Set.of(), "", JwtTokenType.ACCESS_TOKEN, now, now.plusSeconds(1)));
        assertThrows(IllegalArgumentException.class, () -> new JwtClaims("i", "u", Set.of(), "jti", JwtTokenType.ACCESS_TOKEN, now, now));
    }
}
