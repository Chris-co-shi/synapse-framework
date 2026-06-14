package com.indigo.synapse.security.header;

import com.indigo.synapse.core.error.CommonErrorCode;
import com.indigo.synapse.core.exception.SynapseAuthenticationException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrustedHeaderTimestampValidatorTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-14T10:00:00Z"), ZoneOffset.UTC);

    private final TrustedHeaderTimestampValidator validator = new TrustedHeaderTimestampValidator();

    @Test
    void shouldAcceptTimestampWithinTolerance() {
        long timestamp = CLOCK.instant().minus(Duration.ofSeconds(30)).toEpochMilli();

        validator.validate(headers(timestamp), Duration.ofMinutes(1), CLOCK);
    }

    @Test
    void shouldRejectExpiredTimestamp() {
        long timestamp = CLOCK.instant().minus(Duration.ofMinutes(2)).toEpochMilli();

        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> validator.validate(headers(timestamp), Duration.ofMinutes(1), CLOCK)
        );

        assertEquals(CommonErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED, exception.errorCode());
    }

    @Test
    void shouldRejectFutureTimestampOutsideTolerance() {
        long timestamp = CLOCK.instant().plus(Duration.ofMinutes(2)).toEpochMilli();

        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> validator.validate(headers(timestamp), Duration.ofMinutes(1), CLOCK)
        );

        assertEquals(CommonErrorCode.SECURITY_TRUSTED_HEADER_EXPIRED, exception.errorCode());
    }

    @Test
    void shouldRejectInvalidTimestamp() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> validator.validate(Map.of(SecurityHeaders.TIMESTAMP, "invalid"), Duration.ofMinutes(1), CLOCK)
        );

        assertEquals(CommonErrorCode.SECURITY_INVALID_TRUSTED_HEADER, exception.errorCode());
    }

    @Test
    void shouldRejectMissingTimestamp() {
        SynapseAuthenticationException exception = assertThrows(
                SynapseAuthenticationException.class,
                () -> validator.validate(Map.of(), Duration.ofMinutes(1), CLOCK)
        );

        assertEquals(CommonErrorCode.SECURITY_INVALID_TRUSTED_HEADER, exception.errorCode());
    }

    private static Map<String, String> headers(long timestamp) {
        return Map.of(SecurityHeaders.TIMESTAMP, Long.toString(timestamp));
    }
}
