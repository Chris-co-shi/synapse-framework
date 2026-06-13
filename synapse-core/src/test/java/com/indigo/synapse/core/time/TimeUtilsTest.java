package com.indigo.synapse.core.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TimeUtilsTest {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-05-21T08:30:15Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, SHANGHAI);

    @Test
    void shouldReadNowFromClock() {
        assertEquals(FIXED_INSTANT, TimeUtils.now(FIXED_CLOCK));
        assertEquals(LocalDate.of(2026, 5, 21), TimeUtils.nowDate(FIXED_CLOCK));
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 30, 15), TimeUtils.nowDateTime(FIXED_CLOCK));
    }

    @Test
    void shouldConvertBetweenInstantAndLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 5, 21, 12, 0);

        assertEquals(Instant.parse("2026-05-21T04:00:00Z"), TimeUtils.toInstant(dateTime, SHANGHAI));
        assertEquals(dateTime, TimeUtils.toLocalDateTime(Instant.parse("2026-05-21T04:00:00Z"), SHANGHAI));
    }

    @Test
    void shouldBuildDayBoundaries() {
        LocalDate date = LocalDate.of(2026, 5, 21);

        assertEquals(LocalDateTime.of(2026, 5, 21, 0, 0), TimeUtils.startOfDay(date, SHANGHAI));
        assertEquals(LocalDateTime.of(2026, 5, 21, 23, 59, 59, 999_999_999), TimeUtils.endOfDay(date, SHANGHAI));
    }

    @Test
    void shouldRejectNullClock() {
        assertThrows(NullPointerException.class, () -> TimeUtils.now(null));
    }
}
