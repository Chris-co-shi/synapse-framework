package com.indigo.synapse.time;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultTimeRangeConverterTest {

    private final DefaultTimeRangeConverter converter = new DefaultTimeRangeConverter();

    @Test
    void shouldConvertLocalDateToUtcRangeByZone() {
        TimeRange range = converter.dayRange(LocalDate.of(2026, 6, 15), ZoneId.of("Asia/Shanghai"));

        assertThat(range.startInclusive()).isEqualTo(Instant.parse("2026-06-14T16:00:00Z"));
        assertThat(range.endExclusive()).isEqualTo(Instant.parse("2026-06-15T16:00:00Z"));
    }

    @Test
    void shouldConvertLocalDateTimeRangeToUtcRange() {
        TimeRange range = converter.range(
                LocalDateTime.of(2026, 6, 15, 8, 30),
                LocalDateTime.of(2026, 6, 15, 9, 30),
                ZoneId.of("UTC")
        );

        assertThat(range.startInclusive()).isEqualTo(Instant.parse("2026-06-15T08:30:00Z"));
        assertThat(range.endExclusive()).isEqualTo(Instant.parse("2026-06-15T09:30:00Z"));
    }

    @Test
    void shouldRejectInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> converter.range(
                LocalDateTime.of(2026, 6, 15, 10, 0),
                LocalDateTime.of(2026, 6, 15, 10, 0),
                ZoneId.of("UTC")
        ));
    }
}
